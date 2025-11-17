package org.vrglab.imBoredEngine;

import org.junit.jupiter.api.Test;
import org.vrglab.imBoredEngine.core.application.AppData;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AppDataTests {

    @Test
    public void AppData_IsTestEnv_Test() {
        assertEquals(AppData.isTest(), true);
    }

    @Test
    public void AppData_IsDockerEnv_Test() {
        assertEquals(AppData.isDocker(), false);
    }
}
