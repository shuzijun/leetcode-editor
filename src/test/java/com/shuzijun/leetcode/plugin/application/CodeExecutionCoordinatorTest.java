package com.shuzijun.leetcode.plugin.application;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class CodeExecutionCoordinatorTest {

    @Test
    public void rejectsDuplicateKeyButAllowsRunAndSubmitTogether() {
        CodeExecutionCoordinator coordinator = new CodeExecutionCoordinator();

        CodeExecutionCoordinator.Execution run = coordinator.tryStart(
                "two-sum",
                CodeExecutionCoordinator.ExecutionType.RUN
        );
        CodeExecutionCoordinator.Execution submit = coordinator.tryStart(
                "two-sum",
                CodeExecutionCoordinator.ExecutionType.SUBMIT
        );

        assertNotNull(run);
        assertNotNull(submit);
        assertNull(coordinator.tryStart("two-sum", CodeExecutionCoordinator.ExecutionType.RUN));
        assertTrue(coordinator.isActive("two-sum", CodeExecutionCoordinator.ExecutionType.RUN));
        assertTrue(coordinator.isActive("two-sum", CodeExecutionCoordinator.ExecutionType.SUBMIT));
    }

    @Test
    public void exposesStateAndCancellationThroughRequestContext() {
        CodeExecutionCoordinator coordinator = new CodeExecutionCoordinator();
        CodeExecutionCoordinator.Execution execution = coordinator.tryStart(
                "two-sum",
                CodeExecutionCoordinator.ExecutionType.RUN
        );
        assertNotNull(execution);

        assertEquals(CodeExecutionState.STARTING, execution.getState());
        execution.polling();
        assertEquals(CodeExecutionState.POLLING, execution.getState());
        assertFalse(execution.getRequestContext().getCancellationToken().isCancellationRequested());

        assertTrue(coordinator.cancel("two-sum", CodeExecutionCoordinator.ExecutionType.RUN));
        assertEquals(CodeExecutionState.CANCELLED, execution.getState());
        assertTrue(execution.getRequestContext().getCancellationToken().isCancellationRequested());
        assertFalse(coordinator.isActive("two-sum", CodeExecutionCoordinator.ExecutionType.RUN));
    }

    @Test
    public void terminalStateReleasesKeyAndDisposeCancelsRemainingExecutions() {
        CodeExecutionCoordinator coordinator = new CodeExecutionCoordinator();
        CodeExecutionCoordinator.Execution completed = coordinator.tryStart(
                "two-sum",
                CodeExecutionCoordinator.ExecutionType.RUN
        );
        assertNotNull(completed);
        completed.succeeded();
        assertEquals(CodeExecutionState.SUCCEEDED, completed.getState());
        assertFalse(coordinator.isActive("two-sum", CodeExecutionCoordinator.ExecutionType.RUN));

        CodeExecutionCoordinator.Execution active = coordinator.tryStart(
                "add-two-numbers",
                CodeExecutionCoordinator.ExecutionType.SUBMIT
        );
        assertNotNull(active);
        coordinator.dispose();

        assertEquals(CodeExecutionState.CANCELLED, active.getState());
        assertTrue(active.isCancellationRequested());
        assertNull(coordinator.tryStart("three-sum", CodeExecutionCoordinator.ExecutionType.RUN));
    }
}
