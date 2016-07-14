/*
 * openwms.org, the Open Warehouse Management System.
 * Copyright (C) 2014 Heiko Scherrer
 *
 * This file is part of openwms.org.
 *
 * openwms.org is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as 
 * published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * openwms.org is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this software. If not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.openwms.tms;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.io.Serializable;
import java.util.Date;

import org.ameba.integration.jpa.BaseEntity;

/**
 * A TransportOrder is used to move {@code TransportUnit}s from a current {@code Location} to a target.
 *
 * @author <a href="mailto:scherrer@openwms.org">Heiko Scherrer</a>
 * @version 1.0
 * @since 0.1
 */
@Entity
@Table(name = "TMS_TRANSPORT_ORDER")
@NamedQueries({
        @NamedQuery(name = TransportOrder.NQ_FIND_ALL, query = "select to from TransportOrder to order by to.id"),
        @NamedQuery(name = TransportOrder.NQ_FIND_BY_TU, query = "select to from TransportOrder to where to.transportUnit = :transportUnit"),
        @NamedQuery(name = TransportOrder.NQ_FIND_FOR_TU_IN_STATE, query = "select to from TransportOrder to where to.transportUnit = :transportUnit and to.state in (:states)")})
public class TransportOrder extends BaseEntity implements Serializable {

    /**
     * Query to find all {@code TransportOrder}s.
     */
    public static final String NQ_FIND_ALL = "TransportOrder.findAll";

    /**
     * Query to find all {@code TransportOrder}s for a certain {@code TransportUnit}. <li>Query parameter name
     * <strong>transportUnit</strong> : The {@code TransportUnit} to search for.</li>
     */
    public static final String NQ_FIND_BY_TU = "TransportOrder.findByTU";

    /**
     * Query to find all {@code TransportOrder}s for a particular {@code TransportUnit} in certain states. <li>Query parameter name
     * <strong>transportUnit</strong> : The {@code TransportUnit} to search for.</li> <li>Query parameter name <strong>states</strong> : A
     * list of {@link TransportOrder.State}s.</li>
     */
    public static final String NQ_FIND_FOR_TU_IN_STATE = "TransportOrder.findForTuInState";

    /** Unique business key. */
    @Column(name = "C_BK")
    private String bk;

    /**
     * The bk of the {@code TransportUnit} to be moved by this {@code TransportOrder}. Allowed to be {@literal null} to keep {@code
     * TransportOrder}s without {@code TransportUnit}s.
     */
    @Column(name = "C_TRANSPORT_UNIT_BK")
    private String transportUnitBK;

    /**
     * A priority level of the {@code TransportOrder}. The lower the value the lower the priority.<br> The priority level affects the
     * execution of the {@code TransportOrder}. An order with high priority will be processed faster than those with lower priority.
     */
    @Column(name = "C_PRIORITY")
    @Enumerated(EnumType.STRING)
    private PriorityLevel priority = PriorityLevel.NORMAL;

    /**
     * Date when the {@code TransportOrder} was started.
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "C_START_DATE")
    private Date startDate;

    /**
     * Last reported problem on the {@code TransportOrder}.
     */
    @Column(name = "C_PROBLEM")
    private Problem problem;

    /**
     * Date when the {@code TransportOrder} ended.
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "C_END_DATE")
    private Date endDate;

    /**
     * State of the {@code TransportOrder}.
     */
    @Column(name = "C_STATE")
    @Enumerated(EnumType.STRING)
    private TransportOrder.State state = TransportOrder.State.CREATED;

    /**
     * The source {@code Location} of the {@code TransportOrder}.<br> This property is set before the {@code TransportOrder} was started.
     */
    @Column(name = "C_SOURCE_LOCATION")
    private String sourceLocation;

    /**
     * The target {@code Location} of the {@code TransportOrder}.<br> This property is set before the {@code TransportOrder} was started.
     */
    @Column(name = "C_TARGET_LOCATION")
    private String targetLocation;

    /**
     * A {@code LocationGroup} can also be set as target. At least one target must be set when the {@code TransportOrder} is being started.
     */
    @Column(name = "C_TARGET_LOCATION_GROUP")
    private String targetLocationGroup;

    /* ----------------------------- methods ------------------- */

    /**
     * Returns the priority level of the {@code TransportOrder}.
     *
     * @return The priority
     */
    public PriorityLevel getPriority() {
        return this.priority;
    }

    /**
     * Set the priority level of the {@code TransportOrder}.
     *
     * @param priority The priority to set
     */
    public void setPriority(PriorityLevel priority) {
        this.priority = priority;
    }

    /**
     * Returns the date when the {@code TransportOrder} was started.
     *
     * @return The date when started
     */
    public Date getStartDate() {
        return this.startDate;
    }

    /**
     * Get the {@code TransportUnit} assigned to the {@code TransportOrder} .
     *
     * @return The business key of the assigned {@code TransportUnit}
     */
    public String getTransportUnitBK() {
        return this.transportUnitBK;
    }

    /**
     * Assign a {@code TransportUnit} to the {@code TransportOrder}. Setting the {@code TransportUnit} to {@literal null} is allowed here to
     * unlink both.
     *
     * @param transportUnitBK The business key of the {@code TransportUnit} to be assigned
     */
    public void setTransportUnitBK(String transportUnitBK) {
        this.transportUnitBK = transportUnitBK;
    }

    /**
     * Returns the state of the {@code TransportOrder}.
     *
     * @return The state of the order
     */
    public TransportOrder.State getState() {
        return state;
    }

    private void validateInitializationCondition() {
        if (transportUnitBK == null || transportUnitBK.isEmpty() || (targetLocation == null && targetLocationGroup == null)) {
            throw new IllegalStateException("Not all properties set to turn TransportOrder into next state");
        }
    }

    /**
     * Validate whether a state change is valid or not. States must be changed in a defined order. Mostly the order is defined by the
     * ordering if the states in {@link TransportOrderState} enum class. But some other rules are checked here too and an exception is
     * thrown in case the sequence of states is violated.
     *
     * @param newState The new state of the order
     * @throws StateChangeException when <li>newState is {@literal null} or</li><li>the state shall be turned back to a prior state
     * or</li><li>when the caller tries to leap the state {@link TransportOrder.State#INITIALIZED}</li>
     */
    protected void validateStateChange(TransportOrder.State newState) throws StateChangeException {
        if (newState == null) {
            throw new StateChangeException("New TransportState cannot be set to null");
        }
        if (getState().compareTo(newState) > 0) {
            // Don't allow to turn back the state!
            throw new StateChangeException("Turning back the state of a TransportOrder is not allowed");
        }
        if (getState() == TransportOrder.State.CREATED) {
            if (newState != TransportOrder.State.INITIALIZED && newState != TransportOrder.State.CANCELED) {
                // Don't allow to except the initialization
                throw new StateChangeException("A new TransportOrder must first be initialized or be canceled");
            }
            try {
                validateInitializationCondition();
            } catch (IllegalStateException ise) {
                throw new StateChangeException(ise);
            }
        }
    }

    /**
     * Change the state of the {@code TransportOrder} regarding some rules.
     *
     * @param newState The new state of the order
     * @throws StateChangeException in case <ul> <li>the newState is {@literal null} or</li> <li>the newState is less than the old state
     * or</li> <li>the {@code TransportOrder} is in state {@link TransportOrder.State#CREATED} and shall be manually turned into something
     * else then {@link TransportOrder.State#INITIALIZED} or {@link TransportOrder.State#CANCELED}</li> <li>the {@code TransportOrder} is
     * {@link TransportOrder.State#CREATED} and shall be {@link TransportOrder.State#INITIALIZED} but it is incomplete</li> </ul>
     */
    public void setState(TransportOrder.State newState) throws StateChangeException {
        validateStateChange(newState);
        switch (newState) {
            case STARTED:
                startDate = new Date();
                break;
            case FINISHED:
            case ONFAILURE:
            case CANCELED:
                endDate = new Date();
                break;
            default:
                break;
        }
        state = newState;
    }

    /**
     * Get the target {@code Location} of this {@code TransportOrder}.
     *
     * @return The targetLocation if any, otherwise {@literal null}
     */
    public String getTargetLocation() {
        return targetLocation;
    }

    /**
     * Set the target {@code Location} of this {@code TransportOrder}.
     *
     * @param targetLocation The location to move on
     */
    public void setTargetLocation(String targetLocation) {
        this.targetLocation = targetLocation;
    }

    /**
     * Get the targetLocationGroup.
     *
     * @return The targetLocationGroup if any, otherwise {@literal null}
     */
    public String getTargetLocationGroup() {
        return targetLocationGroup;
    }

    /**
     * Set the targetLocationGroup.
     *
     * @param targetLocationGroup The targetLocationGroup to set.
     */
    public void setTargetLocationGroup(String targetLocationGroup) {
        this.targetLocationGroup = targetLocationGroup;
    }

    /**
     * Get the last {@link Problem}.
     *
     * @return The last problem.
     */
    public Problem getProblem() {
        return problem;
    }

    /**
     * Set the last {@link Problem}.
     *
     * @param problem The {@link Problem} to set.
     */
    public void setProblem(Problem problem) {
        this.problem = problem;
    }

    /**
     * Get the endDate.
     *
     * @return The date the order ended
     */
    public Date getEndDate() {
        return endDate;
    }

    /**
     * Get the sourceLocation.
     *
     * @return The sourceLocation
     */
    public String getSourceLocation() {
        return sourceLocation;
    }

    /**
     * Set the sourceLocation.
     *
     * @param sourceLocation The sourceLocation to set
     */
    public void setSourceLocation(String sourceLocation) {
        this.sourceLocation = sourceLocation;
    }

    public static enum State {
        /** Status of new created {@code TransportOrder}s. */
        CREATED(10),

        /** Status of a full initialized {@code TransportOrder}, ready to be started. */
        INITIALIZED(20),

        /** A started and active{@code TransportOrder}, ready to be executed. */
        STARTED(30),

        /** Status to indicate that the {@code TransportOrder} is paused. Not active anymore. */
        INTERRUPTED(40),

        /** Status to indicate a failure on the {@code TransportOrder}. Not active anymore. */
        ONFAILURE(50),

        /** Status of a aborted {@code TransportOrder}. Not active anymore. */
        CANCELED(60),

        /** Status to indicate that the {@code TransportOrder} completed successfully. */
        FINISHED(70);

        private final int order;

        private State(int sortOrder) {
            this.order = sortOrder;
        }

        /**
         * Get the order.
         *
         * @return the order.
         */
        public int getOrder() {
            return order;
        }
    }
}
