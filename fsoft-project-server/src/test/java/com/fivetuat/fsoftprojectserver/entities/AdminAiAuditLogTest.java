package com.fivetuat.fsoftprojectserver.entities;

import com.fivetuat.fsoftprojectserver.enums.ErrorCode;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AdminAiAuditLogTest {

    @Test
    void auditLogBuilderSetsAllFields() {
        AdminAiAuditLog log = AdminAiAuditLog.builder()
                .adminId(UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6"))
                .action("REJECT_DECK")
                .params("{\"deckId\":12,\"reason\":\"spam\"}")
                .status("EXECUTED")
                .resultSummary("Deck rejected")
                .referenceId("12")
                .build();

        assertEquals("REJECT_DECK", log.getAction());
        assertEquals("EXECUTED", log.getStatus());
        assertEquals("12", log.getReferenceId());
    }

    @Test
    void newErrorCodesExist() {
        assertEquals(462, ErrorCode.PENDING_ACTION_NOT_FOUND.getStatus());
        assertEquals(463, ErrorCode.PENDING_ACTION_EXPIRED.getStatus());
    }
}
