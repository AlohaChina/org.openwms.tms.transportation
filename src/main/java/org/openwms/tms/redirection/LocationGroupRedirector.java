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
package org.openwms.tms.redirection;

import org.openwms.common.CommonGateway;
import org.openwms.common.LocationGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * A LocationGroupRedirector votes for a {@link RedirectVote} whether the target locationGroup is enabled for infeed. The class is lazy
 * initialized.
 *
 * @author <a href="mailto:scherrer@openwms.org">Heiko Scherrer</a>
 */
@Lazy
@Order(10)
@Component
class LocationGroupRedirector extends TargetRedirector<LocationGroup> {

    @Autowired
    private CommonGateway commonGateway;

    @Override
    protected boolean isTargetAvailable(LocationGroup target) {
        return !target.isInfeedBlocked();
    }

    @Override
    protected Optional<LocationGroup> resolveTarget(RedirectVote vote) {
        return commonGateway.getLocationGroup(vote.getTarget());
    }

    @Override
    protected void assignTarget(RedirectVote vote) {
        vote.getTransportOrder().setTargetLocationGroup(vote.getTarget());
    }
}