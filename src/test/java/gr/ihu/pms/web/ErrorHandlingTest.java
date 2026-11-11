package gr.ihu.pms.web;

import gr.ihu.pms.billing.BillingService;
import gr.ihu.pms.billing.Invoice;
import gr.ihu.pms.billing.InvoiceLineData;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ErrorHandlingTest {

    @Autowired MockMvc mvc;
    @SpyBean BillingService billing;

    private Invoice issue(long reservationId) {
        return billing.issueForReservation(reservationId, 1L, "Ada Lovelace", "101", 2,
                List.of(new InvoiceLineData("Room 101 · 2 nights", new BigDecimal("230.00"))));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void businessRuleViolationBecomesFlashMessage() throws Exception {
        Invoice inv = issue(2001L);
        mvc.perform(post("/billing/" + inv.getId() + "/pay").param("amount", "500.00").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/billing/" + inv.getId()))
                .andExpect(flash().attribute("error", "Payment exceeds the balance due (230.00)"));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void unknownReservationGives404() throws Exception {
        mvc.perform(get("/reservations/999999")).andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void unknownInvoiceGives404() throws Exception {
        mvc.perform(get("/billing/999999")).andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void paymentOnUnknownInvoiceGives404() throws Exception {
        mvc.perform(post("/billing/999999/pay").param("amount", "10.00").with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void unexpectedErrorIsNotSwallowedAsUserMessage() {
        Invoice inv = issue(2002L);
        doThrow(new NullPointerException("internal detail"))
                .when(billing).recordPayment(any(), any(), any());
        assertThatThrownBy(() ->
                mvc.perform(post("/billing/" + inv.getId() + "/pay").param("amount", "10.00").with(csrf())))
                .hasRootCauseInstanceOf(NullPointerException.class);
    }

    @Test
    @WithMockUser(roles = "RECEPTIONIST")
    void receptionistCannotOpenBilling() throws Exception {
        mvc.perform(get("/billing")).andExpect(status().isForbidden());
    }
}
