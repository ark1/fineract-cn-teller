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
package org.apache.fineract.cn.teller.service.internal.service.helper;

import org.apache.fineract.cn.teller.ServiceConstants;
import org.apache.fineract.cn.api.util.NotFoundException;
import org.apache.fineract.cn.cheque.api.v1.client.ChequeManager;
import org.apache.fineract.cn.cheque.api.v1.domain.ChequeTransaction;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class ChequeService {

  private final Logger logger;
  private final ChequeManager chequeManager;

  @Autowired
  public ChequeService(@Qualifier(ServiceConstants.LOGGER_NAME) final Logger logger,
                       final ChequeManager chequeManager) {
    super();
    this.logger = logger;
    this.chequeManager = chequeManager;
  }

  public void process(final ChequeTransaction chequeTransaction) {
    this.chequeManager.process(chequeTransaction);
  }

  public boolean chequeExists(final String identifier) {
    try {
      this.chequeManager.get(identifier);
      return true;
    } catch (final NotFoundException nfex) {
      return false;
    }
  }
}
