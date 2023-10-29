package kitchenpos.ordertable.application;

import kitchenpos.ordertable.application.validator.OrderStatusValidator;
import kitchenpos.ordertable.OrderTable;
import kitchenpos.ordertable.application.dto.request.OrderTableChangeEmptyRequest;
import kitchenpos.ordertable.application.dto.request.OrderTableChangeGuestNumberRequest;
import kitchenpos.ordertable.application.dto.request.OrderTableCreateRequest;
import kitchenpos.ordertable.application.dto.response.OrderTableResponse;
import kitchenpos.ordertable.repository.OrderTableRepository;
import kitchenpos.ordertable.vo.NumberOfGuests;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Transactional(readOnly = true)
@Service
public class TableService {

    private final OrderTableRepository orderTableRepository;
    private final OrderStatusValidator orderStatusValidator;

    public TableService(final OrderTableRepository orderTableRepository, final OrderStatusValidator orderStatusValidator) {
        this.orderTableRepository = orderTableRepository;
        this.orderStatusValidator = orderStatusValidator;
    }

    @Transactional
    public OrderTableResponse create(final OrderTableCreateRequest numberOfGuestsAndEmptyStatus) {
        final OrderTable orderTable = new OrderTable(
                new NumberOfGuests(numberOfGuestsAndEmptyStatus.getNumberOfGuests()),
                numberOfGuestsAndEmptyStatus.isEmpty()
        );
        return OrderTableResponse.from(orderTableRepository.save(orderTable));
    }

    public List<OrderTableResponse> list() {
        return orderTableRepository.findAll().stream()
                .map(OrderTableResponse::from)
                .collect(Collectors.toUnmodifiableList());
    }

    @Transactional
    public OrderTableResponse changeEmpty(final Long orderTableId, final OrderTableChangeEmptyRequest emptyInput) {
        final OrderTable orderTable = orderTableRepository.findMandatoryById(orderTableId);
        if (!orderTable.isEmpty()) {
            orderStatusValidator.validateCompletion(orderTable);
        }
        orderTable.changeEmpty(emptyInput.getEmpty());
        return OrderTableResponse.from(orderTableRepository.save(orderTable));
    }

    @Transactional
    public OrderTableResponse changeNumberOfGuests(
            final Long orderTableId,
            final OrderTableChangeGuestNumberRequest numberOfGuestsInput
    ) {
        final NumberOfGuests numberOfGuests = new NumberOfGuests(numberOfGuestsInput.getNumberOfGuests());
        final OrderTable orderTable = orderTableRepository.findMandatoryById(orderTableId);
        orderTable.changeNumberOfGuests(numberOfGuests);
        return OrderTableResponse.from(orderTableRepository.save(orderTable));
    }
}