package kitchenpos.application;

import kitchenpos.domain.OrderTable;
import kitchenpos.domain.OrderTables;
import kitchenpos.domain.TableGroup;
import kitchenpos.repository.OrderRepository;
import kitchenpos.repository.OrderTableRepository;
import kitchenpos.repository.TableGroupRepository;
import kitchenpos.application.dto.request.TableGroupCreateRequest;
import kitchenpos.application.dto.response.TableGroupResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Transactional(readOnly = true)
@Service
public class TableGroupService {

    private final OrderRepository orderRepository;
    private final OrderTableRepository orderTableRepository;
    private final TableGroupRepository tableGroupRepository;

    public TableGroupService(
            final OrderRepository orderRepository,
            final OrderTableRepository orderTableRepository,
            final TableGroupRepository tableGroupRepository
    ) {
        this.orderRepository = orderRepository;
        this.orderTableRepository = orderTableRepository;
        this.tableGroupRepository = tableGroupRepository;
    }

    @Transactional
    public TableGroupResponse create(final TableGroupCreateRequest orderTableIds) {
        final List<OrderTable> orderTablesResult = orderTableRepository.findAllById(orderTableIds.getOrderTableIds());
        final OrderTables orderTables = new OrderTables(orderTablesResult);
        validateTableGroupInput(orderTableIds.getOrderTableIds(), orderTables);
        orderTables.changeToAllOccupied();
        final TableGroup savedTableGroup = tableGroupRepository.save(new TableGroup(orderTables));
        return TableGroupResponse.from(savedTableGroup);
    }

    private void validateTableGroupInput(final List<Long> idsInput, final OrderTables savedOrderTables) {
        if (CollectionUtils.isEmpty(idsInput) || idsInput.size() < 2) {
            throw new IllegalArgumentException();
        }
        if (!savedOrderTables.hasSizeOf(idsInput.size())) {
            throw new IllegalArgumentException();
        }
    }

    @Transactional
    public void ungroup(final Long tableGroupId) {
        final TableGroup tableGroup = tableGroupRepository.findMandatoryById(tableGroupId);
        final List<OrderTable> orderTablesResult = orderTableRepository.findByTableGroup(tableGroup);
        final OrderTables orderTables = new OrderTables(orderTablesResult);
        final Long completionOrderCount = orderRepository.countCompletionOrderByOrderTableIds(orderTables.getIds());
        orderTables.validateSizeAndUngroup(completionOrderCount.intValue());
        orderTableRepository.saveAll(orderTables.getOrderTables());
    }
}
