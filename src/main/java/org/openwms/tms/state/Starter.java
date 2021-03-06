/*
 * Copyright 2018 Heiko Scherrer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openwms.tms.state;

import org.ameba.exception.NotFoundException;
import org.openwms.common.CommonGateway;
import org.openwms.common.Location;
import org.openwms.common.LocationGroup;
import org.openwms.tms.StateChangeException;
import org.openwms.tms.TransportOrder;
import org.openwms.tms.TransportOrderRepository;
import org.openwms.tms.TransportOrderState;
import org.openwms.tms.TransportServiceEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * A Starter.
 *
 * @author <a href="mailto:scherrer@openwms.org">Heiko Scherrer</a>
 */
//@Transactional(propagation = Propagation.MANDATORY)
@Component
class Starter implements ApplicationListener<TransportServiceEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(Starter.class);
    private final TransportOrderRepository repository;
    private final CommonGateway commonGateway;

    Starter(TransportOrderRepository repository, CommonGateway commonGateway) {
        this.repository = repository;
        this.commonGateway = commonGateway;
    }

    /**
     * Handle an application event.
     *
     * @param event the event to respond to
     */
    @Override
    public void onApplicationEvent(TransportServiceEvent event) {
        final TransportOrder to = repository.findById((Long) event.getSource()).orElseThrow(NotFoundException::new);
        switch (event.getType()) {
            case INITIALIZED:
                start(to);
                break;
            case TRANSPORT_FINISHED:
            case TRANSPORT_ONFAILURE:
            case TRANSPORT_CANCELED:
            case TRANSPORT_INTERRUPTED:
                startNext(to);
                break;
            default:
                // just accept the evolution here
        }
    }

    private void startNext(TransportOrder to) {
        List<TransportOrder> transportOrders = repository.findByTransportUnitBKAndStates(to.getTransportUnitBK(), TransportOrderState.INITIALIZED);
        if (!transportOrders.isEmpty()) {
            start(transportOrders.get(0));
        }
    }

    private void start(TransportOrder to) {
        LOGGER.debug("> Request to start the TransportOrder with PKey [{}]", to.getPersistentKey());
        Optional<LocationGroup> lg = commonGateway.getLocationGroup(to.getTargetLocationGroup());
        Optional<Location> loc = to.getTargetLocation() == null ? Optional.empty() : commonGateway.getLocation(to.getTargetLocation());
        if (!lg.isPresent() && !loc.isPresent()) {
            // At least one target must be set
            throw new NotFoundException("Neither a valid target LocationGroup nor a Location are set, hence it is not possible to start the TransportOrder");
        }
        if (lg.isPresent()) {
            if (lg.get().isInfeedBlocked()) {
                throw new StateChangeException("Cannot start the TransportOrder because TargetLocationGroup is blocked");
            }
            to.setTargetLocationGroup(lg.get().asString());
        } else {
            to.setTargetLocationGroup(null);
        }
        if (loc.isPresent()) {
            if (loc.get().isInfeedBlocked()) {
                throw new StateChangeException("Cannot start the TransportOrder because TargetLocation is blocked");
            }
            to.setTargetLocation(loc.get().asString());
        } else {
            to.setTargetLocation(null);
        }

        List<TransportOrder> others = repository.findByTransportUnitBKAndStates(to.getTransportUnitBK(), TransportOrderState.STARTED);
        if (!others.isEmpty()) {
            throw new StateChangeException("Cannot start TransportOrder for TransportUnit [" + to.getTransportUnitBK() + "] because " + others.size() + " TransportOrders already started [" + others.get(0).getPersistentKey() + "]");
        }
        to.changeState(TransportOrderState.STARTED);
        repository.save(to);
        LOGGER.info("TransportOrder for TransportUnit with Barcode {} STARTED at {}. Persisted key is {}", to.getTransportUnitBK(), to.getStartDate(), to.getPk());
    }
}
