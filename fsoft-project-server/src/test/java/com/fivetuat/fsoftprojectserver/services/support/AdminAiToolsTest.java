package com.fivetuat.fsoftprojectserver.services.support;

import com.fivetuat.fsoftprojectserver.dtos.PageResponse;
import com.fivetuat.fsoftprojectserver.dtos.response.AdminDashboardStatsResponse;
import com.fivetuat.fsoftprojectserver.dtos.response.DeckResponse;
import com.fivetuat.fsoftprojectserver.services.AdminDashboardService;
import com.fivetuat.fsoftprojectserver.services.AdminDeckService;
import com.fivetuat.fsoftprojectserver.services.AdminUserService;
import com.fivetuat.fsoftprojectserver.utils.SecurityUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminAiToolsTest {

    @Mock AdminDeckService adminDeckService;
    @Mock AdminUserService adminUserService;
    @Mock AdminDashboardService adminDashboardService;
    @Mock PendingActionStore pendingActionStore;

    UUID adminId = UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6");

    private AdminAiTools tools() {
        return new AdminAiTools(
                adminDeckService, adminUserService, adminDashboardService,
                null, null, pendingActionStore);
    }

    @Test
    void dashboardStatsReturnsFormattedString() {
        when(adminDashboardService.getDashboardStats()).thenReturn(
                AdminDashboardStatsResponse.builder()
                        .totalUsers(10).totalDecks(20).totalCards(300)
                        .totalGameRecords(5).totalOfficialDecks(3).totalPublicDecks(8)
                        .build());

        String out = tools().dashboardStats();

        assertTrue(out.contains("10"));
        assertTrue(out.contains("20"));
    }

    @Test
    void pendingDecksReturnsEmptyMessage() {
        when(adminDeckService.getPendingPublicDecks(any())).thenReturn(
                PageResponse.<DeckResponse>builder()
                        .content(List.of()).totalElements(0).totalPages(0)
                        .pageNo(0).pageSize(10).last(true).build());

        String out = tools().pendingDecks(1, 10);

        assertTrue(out.contains("Không có bộ bài nào đang chờ duyệt"));
    }

    @Test
    void requestRejectDeckStoresPendingActionAndNeverExecutes() {
        String out;
        try (MockedStatic<SecurityUtils> mocked = mockStatic(SecurityUtils.class)) {
            mocked.when(SecurityUtils::getCurrentUserLogin).thenReturn(Optional.of(adminId));

            out = tools().requestRejectDeck(12L, "nội dung không phù hợp");
        }

        assertTrue(out.startsWith("PENDING_CONFIRMATION: actionId="));
        verify(adminDeckService, never()).approvePublicDeck(anyLong(), anyBoolean(), any());
    }

    @Test
    void requestUpdateUserRolesStoresPendingAction() {
        String out;
        try (MockedStatic<SecurityUtils> mocked = mockStatic(SecurityUtils.class)) {
            mocked.when(SecurityUtils::getCurrentUserLogin).thenReturn(Optional.of(adminId));

            out = tools().requestUpdateUserRoles(
                    "3fa85f64-5717-4562-b3fc-2c963f66afa6", "USER,ADMIN");
        }

        assertTrue(out.startsWith("PENDING_CONFIRMATION: actionId="));
        verify(adminUserService, never()).updateUserRoles(any(), any());
    }
}