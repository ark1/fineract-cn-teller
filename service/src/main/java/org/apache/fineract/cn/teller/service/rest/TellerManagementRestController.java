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
package org.apache.fineract.cn.teller.service.rest;

import org.apache.fineract.cn.teller.ServiceConstants;
import org.apache.fineract.cn.teller.api.v1.PermittableGroupIds;
import org.apache.fineract.cn.teller.api.v1.domain.Teller;
import org.apache.fineract.cn.teller.api.v1.domain.TellerBalanceSheet;
import org.apache.fineract.cn.teller.api.v1.domain.TellerDenomination;
import org.apache.fineract.cn.teller.api.v1.domain.TellerManagementCommand;
import org.apache.fineract.cn.teller.service.internal.command.ChangeTellerCommand;
import org.apache.fineract.cn.teller.service.internal.command.CloseTellerCommand;
import org.apache.fineract.cn.teller.service.internal.command.CreateTellerCommand;
import org.apache.fineract.cn.teller.service.internal.command.DeleteTellerCommand;
import org.apache.fineract.cn.teller.service.internal.command.OpenTellerCommand;
import org.apache.fineract.cn.teller.service.internal.command.TellerDenominationCommand;
import org.apache.fineract.cn.teller.service.internal.service.TellerManagementService;
import org.apache.fineract.cn.teller.service.internal.service.helper.AccountingService;
import org.apache.fineract.cn.teller.service.internal.service.helper.OrganizationService;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import javax.validation.Valid;
import org.apache.fineract.cn.anubis.annotation.AcceptedTokenType;
import org.apache.fineract.cn.anubis.annotation.Permittable;
import org.apache.fineract.cn.command.gateway.CommandGateway;
import org.apache.fineract.cn.lang.DateConverter;
import org.apache.fineract.cn.lang.DateRange;
import org.apache.fineract.cn.lang.ServiceException;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/offices/{officeIdentifier}/teller")
public class TellerManagementRestController {

  private final Logger logger;
  private final CommandGateway commandGateway;
  private final TellerManagementService tellerManagementService;
  private final OrganizationService organizationService;
  private final AccountingService accountingService;

  @Autowired
  public TellerManagementRestController(@Qualifier(ServiceConstants.LOGGER_NAME) final Logger logger,
                                        final CommandGateway commandGateway,
                                        final TellerManagementService tellerManagementService,
                                        final OrganizationService organizationService,
                                        final AccountingService accountingService) {
    super();
    this.logger = logger;
    this.commandGateway = commandGateway;
    this.tellerManagementService = tellerManagementService;
    this.organizationService = organizationService;
    this.accountingService = accountingService;
  }

  @Permittable(value = AcceptedTokenType.TENANT, groupId = PermittableGroupIds.TELLER_MANAGEMENT)
  @RequestMapping(
      value = "",
      method = RequestMethod.POST,
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE
  )
  @ResponseBody
  ResponseEntity<Void> create(@PathVariable("officeIdentifier") final String officeIdentifier,
                              @RequestBody @Valid final Teller teller) {
    if (this.tellerManagementService.findByIdentifier(teller.getCode()).isPresent()) {
      throw ServiceException.conflict("Teller {0} already exists.", teller.getCode());
    }

    this.verifyOffice(officeIdentifier);
    this.verifyAccount(teller.getTellerAccountIdentifier());
    this.verifyAccount(teller.getVaultAccountIdentifier());
    this.verifyAccount(teller.getChequesReceivableAccount());
    this.verifyAccount(teller.getCashOverShortAccount());

    this.commandGateway.process(new CreateTellerCommand(officeIdentifier, teller));

    return ResponseEntity.accepted().build();
  }

  @Permittable(value = AcceptedTokenType.TENANT, groupId = PermittableGroupIds.TELLER_MANAGEMENT)
  @RequestMapping(
      value = "/{tellerCode}",
      method = RequestMethod.GET,
      consumes = MediaType.ALL_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE
  )
  @ResponseBody
  ResponseEntity<Teller> find(@PathVariable("officeIdentifier") final String officeIdentifier,
                              @PathVariable("tellerCode") final String tellerCode) {
    this.verifyOffice(officeIdentifier);

    return ResponseEntity.ok(
        this.tellerManagementService.findByIdentifier(tellerCode)
            .orElseThrow(() -> ServiceException.notFound("Teller {0} not found.", tellerCode))
    );
  }

  @Permittable(value = AcceptedTokenType.TENANT, groupId = PermittableGroupIds.TELLER_MANAGEMENT)
  @RequestMapping(
      value = "",
      method = RequestMethod.GET,
      consumes = MediaType.ALL_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE
  )
  @ResponseBody
  ResponseEntity<List<Teller>> fetch(@PathVariable("officeIdentifier") final String officeIdentifier) {
    this.verifyOffice(officeIdentifier);

    return ResponseEntity.ok(
        this.tellerManagementService.findByOfficeIdentifier(officeIdentifier)
    );
  }

  @Permittable(value = AcceptedTokenType.TENANT, groupId = PermittableGroupIds.TELLER_MANAGEMENT)
  @RequestMapping(
      value = "/{tellerCode}",
      method = RequestMethod.PUT,
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE
  )
  @ResponseBody
  ResponseEntity<Void> change(@PathVariable("officeIdentifier") final String officeIdentifier,
                              @PathVariable("tellerCode") final String tellerCode,
                              @RequestBody @Valid final Teller teller) {
    if (!tellerCode.equals(teller.getCode())) {
      throw ServiceException.badRequest("Teller code {0} must much given teller.", tellerCode);
    }

    this.verifyTeller(tellerCode);
    this.verifyOffice(officeIdentifier);
    this.verifyAccount(teller.getTellerAccountIdentifier());
    this.verifyAccount(teller.getVaultAccountIdentifier());
    this.verifyAccount(teller.getChequesReceivableAccount());
    this.verifyAccount(teller.getCashOverShortAccount());

    this.commandGateway.process(new ChangeTellerCommand(officeIdentifier, teller));

    return ResponseEntity.accepted().build();
  }

  @Permittable(value = AcceptedTokenType.TENANT, groupId = PermittableGroupIds.TELLER_MANAGEMENT)
  @RequestMapping(
      value = "/{tellerCode}/commands",
      method = RequestMethod.POST,
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE
  )
  @ResponseBody
  ResponseEntity<Void> post(@PathVariable("officeIdentifier") final String officeIdentifier,
                            @PathVariable("tellerCode") final String tellerCode,
                            @RequestBody @Valid final TellerManagementCommand tellerManagementCommand) {
    this.verifyOffice(officeIdentifier);
    final Teller teller = this.verifyTeller(tellerCode);

    final TellerManagementCommand.Action action = TellerManagementCommand.Action.valueOf(tellerManagementCommand.getAction());
    switch (action) {
      case OPEN:
        if (!teller.getState().equals(Teller.State.CLOSED.name())) {
          throw ServiceException.badRequest("Teller {0} is already active.", tellerCode);
        }
        this.verifyEmployee(tellerManagementCommand.getAssignedEmployeeIdentifier());
        this.commandGateway.process(new OpenTellerCommand(tellerCode, tellerManagementCommand));
        break;
      case CLOSE:
        if (teller.getState().equals(Teller.State.CLOSED.name())) {
          throw ServiceException.badRequest("Teller {0} is already closed.", tellerCode);
        }
        if (teller.getDenominationRequired()) {
          final LocalDateTime lastOpenedOn = DateConverter.fromIsoString(teller.getLastOpenedOn());
          if (this.tellerManagementService
              .fetchTellerDenominations(tellerCode, lastOpenedOn, LocalDateTime.now(Clock.systemUTC()))
              .isEmpty()) {
            throw ServiceException.conflict("Denomination for teller {0} required.", tellerCode);
          }
        }
        this.commandGateway.process(new CloseTellerCommand(tellerCode, tellerManagementCommand));
        break;
      default:
        throw ServiceException.badRequest("Unsupported teller command {0}.", action.name());
    }

    return ResponseEntity.accepted().build();
  }

  @Permittable(value = AcceptedTokenType.TENANT, groupId = PermittableGroupIds.TELLER_MANAGEMENT)
  @RequestMapping(
      value = "/{tellerCode}/balance",
      method = RequestMethod.GET,
      consumes = MediaType.ALL_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE
  )
  @ResponseBody
  ResponseEntity<TellerBalanceSheet> getBalance(@PathVariable("officeIdentifier") final String officeIdentifier,
                                                @PathVariable("tellerCode") final String tellerCode) {
    this.verifyOffice(officeIdentifier);
    this.verifyTeller(tellerCode);

    return ResponseEntity.ok(this.tellerManagementService.getBalance(tellerCode));
  }

  @Permittable(value = AcceptedTokenType.TENANT, groupId = PermittableGroupIds.TELLER_MANAGEMENT)
  @RequestMapping(
      value = "/{tellerCode}",
      method = RequestMethod.DELETE,
      consumes = MediaType.ALL_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE
  )
  public
  @ResponseBody
  ResponseEntity<Void> deleteTeller(@PathVariable("officeIdentifier") final String officeIdentifier,
                                    @PathVariable("tellerCode") final String tellerCode) {
    this.verifyOffice(officeIdentifier);
    final Teller teller = this.verifyTeller(tellerCode);
    if (teller.getLastOpenedBy() != null) {
      throw ServiceException.conflict("Teller {0} already used.", tellerCode);
    }

    this.commandGateway.process(new DeleteTellerCommand(tellerCode));

    return ResponseEntity.accepted().build();
  }

  @Permittable(value = AcceptedTokenType.TENANT, groupId = PermittableGroupIds.TELLER_MANAGEMENT)
  @RequestMapping(
      value = "/{tellerCode}/denominations",
      method = RequestMethod.POST,
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE
  )
  public
  @ResponseBody
  ResponseEntity<Void> saveTellerDenomination(@PathVariable("officeIdentifier") final String officeIdentifier,
                                              @PathVariable("tellerCode") final String tellerCode,
                                              @RequestBody @Valid final TellerDenomination tellerDenomination) {
    this.verifyOffice(officeIdentifier);
    final Teller teller = this.verifyTeller(tellerCode);
    if (!teller.getState().equals(Teller.State.PAUSED.name())) {
      throw ServiceException.conflict("Teller {0} is still in use.", tellerCode);
    }

    if (teller.getCashOverShortAccount() == null) {
      throw ServiceException.badRequest("Cash over/short account for teller {0} not set.", tellerCode);
    }

    this.verifyAccount(teller.getCashOverShortAccount());

    final TellerBalanceSheet tellerBalanceSheet = this.tellerManagementService.getBalance(tellerCode);

    this.commandGateway.process(
        new TellerDenominationCommand(tellerCode, tellerBalanceSheet.getCashOnHand(), tellerDenomination));

    return ResponseEntity.accepted().build();
  }

  @Permittable(value = AcceptedTokenType.TENANT, groupId = PermittableGroupIds.TELLER_MANAGEMENT)
  @RequestMapping(
      value = "/{tellerCode}/denominations",
      method = RequestMethod.GET,
      consumes = MediaType.ALL_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE
  )
  public
  @ResponseBody
  ResponseEntity<List<TellerDenomination>> fetchTellerDenominations(
      @PathVariable("officeIdentifier") final String officeIdentifier,
      @PathVariable("tellerCode") final String tellerCode,
      @RequestParam(value = "dateRange", required = false) final String dateRange) {
    this.verifyOffice(officeIdentifier);
    this.verifyTeller(tellerCode);

    final DateRange dateRangeHolder;
    if (dateRange != null) {
      dateRangeHolder = DateRange.fromIsoString(dateRange);
    } else {
      final LocalDate now = LocalDate.now();
      dateRangeHolder = new DateRange(now.minusMonths(1L).plusDays(1L), now);
    }
    return ResponseEntity.ok(
        this.tellerManagementService.fetchTellerDenominations(
            tellerCode, dateRangeHolder.getStartDateTime(), dateRangeHolder.getEndDateTime())
    );
  }

  private void verifyAccount(final String accountIdentifier) {
    if (!this.accountingService.findAccount(accountIdentifier).isPresent()) {
      throw ServiceException.badRequest("Account {0} not found.", accountIdentifier);
    }
  }

  private void verifyEmployee(final String employeeIdentifier) {
    final Optional<Teller> optionalTeller = this.tellerManagementService.findByAssignedEmployee(employeeIdentifier);
    if (optionalTeller.isPresent()) {
      throw ServiceException.conflict("Employee {0} already assigned to teller {1}.",
          employeeIdentifier, optionalTeller.get().getCode());
    }

    if (!this.organizationService.employeeExists(employeeIdentifier)) {
      throw ServiceException.badRequest("Employee {0} not found.", employeeIdentifier);
    }
  }

  private void verifyOffice(final String officeIdentifier) {
    if (!this.organizationService.officeExists(officeIdentifier)) {
      throw ServiceException.badRequest("Office {0} not found.", officeIdentifier);
    }
  }

  private Teller verifyTeller(final String tellerCode) {
    final Optional<Teller> optionalTeller = this.tellerManagementService.findByIdentifier(tellerCode);
    if (!optionalTeller.isPresent()) {
      throw ServiceException.notFound("Teller {0} not found.", tellerCode);
    }
    return optionalTeller.get();
  }
}
