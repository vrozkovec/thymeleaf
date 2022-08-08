/*
 * =============================================================================
 * 
 *   Copyright (c) 2011-2016, The THYMELEAF team (http://www.thymeleaf.org)
 * 
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 * 
 * =============================================================================
 */
package org.thymeleaf.templateengine.conversion.conversion1;

import java.util.Arrays;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.thymeleaf.TestExecutorFactory;
import org.thymeleaf.dialect.IDialect;
import org.thymeleaf.testing.templateengine.engine.TestExecutor;


public class Conversion1Test {



    public Conversion1Test() {
        super();
    }






    @Test
    public void testConversion() throws Exception {

        final TestExecutor executor = TestExecutorFactory.createTestExecutor();
        executor.setDialects(Arrays.asList(new IDialect[]{new ConversionTestDialect1()}));

        executor.execute("classpath:templateengine/conversion/conversion1");

        Assertions.assertTrue(executor.isAllOK());

    }


}
