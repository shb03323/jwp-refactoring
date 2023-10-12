package kitchenpos.application;

import kitchenpos.application.config.ServiceTestConfig;
import kitchenpos.domain.OrderTable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

class TableServiceTest extends ServiceTestConfig {

    private TableService tableService;

    @BeforeEach
    void setUp() {
        tableService = new TableService(orderDao, orderTableDao);
    }

    @DisplayName("주문 테이블 생성")
    @Nested
    class Create {
        @DisplayName("성공한다.")
        @Test
        void success() {
            // given
            final OrderTable orderTableInput = new OrderTable();
            orderTableInput.setNumberOfGuests(1);
            orderTableInput.setEmpty(true);

            // when
            final OrderTable actual = tableService.create(orderTableInput);

            // then
            assertSoftly(softly -> {
                softly.assertThat(actual.getNumberOfGuests()).isEqualTo(orderTableInput.getNumberOfGuests());
                softly.assertThat(actual.isEmpty()).isEqualTo(orderTableInput.isEmpty());
            });
        }
    }
}
