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
package org.apache.fineract.cn.teller;

public interface ServiceConstants {
  String LOGGER_NAME = "teller-logger";

  int ITERATION_COUNT = 2048;
  int LENGTH = 2048;

  String TX_OPEN_ACCOUNT = "ACCO";
  String TX_CLOSE_ACCOUNT = "ACCC";
  String TX_ACCOUNT_TRANSFER = "ACCT";
  String TX_CASH_DEPOSIT = "CDPT";
  String TX_CASH_WITHDRAWAL = "CWDL";
  String TX_REPAYMENT = "PPAY";
  String TX_CHEQUE = "CCHQ";

  String TX_DEPOSIT_ADJUSTMENT = "DAJT";
  String TX_CREDIT_ADJUSTMENT = "CAJT";
  String TX_CHARGES = "CHRG";
}
