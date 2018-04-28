/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.cn.teller.api.v1.domain;

import java.math.BigDecimal;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import org.apache.fineract.cn.lang.validation.constraints.ValidIdentifier;

public class TellerManagementCommand {

  public enum Action {
    OPEN,
    CLOSE
  }

  public enum Adjustment {
    NONE,
    DEBIT,
    CREDIT
  }

  @NotNull
  private Action action;
  @NotNull
  private Adjustment adjustment;
  @DecimalMin("0.00")
  @DecimalMax("9999999999.99999")
  private BigDecimal amount;
  @ValidIdentifier(optional = true)
  private String assignedEmployeeIdentifier;

  public TellerManagementCommand() {
    super();
  }

  public String getAction() {
    return this.action.name();
  }

  public void setAction(final String action) {
    this.action = Action.valueOf(action);
  }

  public String getAdjustment() {
    return this.adjustment.name();
  }

  public void setAdjustment(final String adjustment) {
    this.adjustment = Adjustment.valueOf(adjustment);
  }

  public BigDecimal getAmount() {
    return this.amount;
  }

  public void setAmount(final BigDecimal amount) {
    this.amount = amount;
  }

  public String getAssignedEmployeeIdentifier() {
    return this.assignedEmployeeIdentifier;
  }

  public void setAssignedEmployeeIdentifier(final String assignedEmployeeIdentifier) {
    this.assignedEmployeeIdentifier = assignedEmployeeIdentifier;
  }
}
