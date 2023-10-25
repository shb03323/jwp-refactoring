package kitchenpos.tablegroup.application;

import kitchenpos.ordertable.OrderTable;
import kitchenpos.ordertable.OrderTables;
import kitchenpos.tablegroup.TableGroup;
import kitchenpos.order.repository.OrderRepository;
import kitchenpos.ordertable.repository.OrderTableRepository;
import kitchenpos.tablegroup.repository.TableGroupRepository;
import kitchenpos.tablegroup.application.dto.request.TableGroupCreateRequest;
import kitchenpos.tablegroup.application.dto.response.TableGroupResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;

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
        orderTables.validateCanGroupAndChangeToOccupied();
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