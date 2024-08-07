<?php
/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
namespace Test;

class GH7546 {
    private TestClass $testClass1;
    private NS1\Sub1\TestClass $testClass2;
    private TestInterface $testInterface;
    private \NS1\Sub1\Sub2\Sub3\TestInterface $testInterface2;

    public const CONSTANT = TestEnum::Test;
    public const int CONSTANT2 = CONSTANT;

    use TestTrait;

    public function test(): void {
        func();
    }
}
