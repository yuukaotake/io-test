/**
 * personium.io
 * Copyright 2014 FUJITSU LIMITED
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.fujitsu.dc.client.test;

import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;

/**
 * IT用テストランナークラス.
 */
/**
 * This is the IT test runner class.
 */
public class DcRunner extends BlockJUnit4ClassRunner {
    /**
     * コンストラクタ.
     * @param klass klass
     * @throws InitializationError InitializationError
     */
    /**
     * This is the parameterized constructor calling its parent constructor internally.
     * @param klass Class
     * @throws InitializationError InitializationError Exception
     */
    public DcRunner(Class<?> klass) throws InitializationError {
        super(klass);
    }

    /**
     * This method calls its overridden version of super class.
     * @param method .
     * @param notifier .
     */
    @Override
    protected void runChild(FrameworkMethod method, RunNotifier notifier) {
        System.out.println("■■■■ " + method.getName() + " ■■■■");
        super.runChild(method, notifier);
    }
}
