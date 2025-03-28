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

$anon = new class() {
    public function __construct(
        // set visibility
        public(set) $publicSetInvalid, // error1
        public(set) string $publicSet, // implicit public
        protected(set) string $protectedSet, // implicit public
        private(set) string $privateSet, // implicit public, implicit final
        // set visibility, readonly
        public(set) readonly string $publicSetReadonly, // implicit public
        readonly public(set) string $readonlyPublicSet, // implicit public
        protected(set) readonly string $protectedSetReadonly, // implicit public
        readonly protected(set) string $readonlyProtectedSet, // implicit public
        private(set) readonly string $privateSetReadonly, // implicit public
        readonly private(set) string $readonlyPrivateSet, // implicit public
        // visibility, set visibility
        public public(set) int $publicPublicSet,
        public protected(set) int $publicProtectedSet,
        public private(set) int $publicPrivateSet,
        protected public(set) int $protectedPublicSet, // error2 PHP Fatal error:  Visibility of property $protectedPublicSet must not be weaker than set visibility
        protected protected(set) int $protectedProtectedSet,
        protected private(set) int $protectedPrivateSet,
        private public(set) int $privatePublicSet, // error3
        private protected(set) int $privateProtectedSet, // error4
        private private(set) int $privatePrivateSet,
        public(set) public int $publicSetPublic,
        public(set) protected int $publicSetProtected, // error5
        public(set) private int $publicSetPrivate, // error6
        protected(set) public int $protectedSetPublic,
        protected(set) protected int $protectedSetProtected,
        protected(set) private int $protectedSetPrivate, // error7
        private(set) public int $privateSetPublic,
        private(set) protected int $privateSetProtected,
        private(set) private int $privateSetPrivate,
        // visibility, set visibility, readonly
        public public(set) readonly int $publicPublicSetReadonly,
        public protected(set) readonly int $publicProtectedSetReadonly,
        public private(set) readonly int $publicPrivateSetReadonly,
        public readonly public(set) int $publicReadonlyPublicSet,
        public readonly protected(set) int $publicReadonlyProtectedSet,
        public readonly private(set) int $publicReadonlyPrivateSet,
        protected public(set) readonly int $protectedPublicSetReadonly, // error8
        protected protected(set) readonly int $protectedProtectedSetReadonly,
        protected private(set) readonly int $protectedPrivateSetReadonly,
        protected readonly public(set) int $protectedReadonlyPublicSet, // error9
        protected readonly protected(set) int $protectedReadonlyProtectedSet,
        protected readonly private(set) int $protectedReadonlyPrivateSet,
        private public(set) readonly int $privatePublicSetReadonly, // error10
        private protected(set) readonly int $privateProtectedSetReadonly, // error11
        private private(set) readonly int $privatePrivateSetReadonly,
        private readonly public(set) int $privateReadonlyPublicSet, // error12
        private readonly protected(set) int $privateReadonlyProtectedSet, // error13
        private readonly private(set) int $privateReadonlyPrivateSet,
        public(set) public readonly string $publicSetPublicReadonly,
        public(set) protected readonly string $publicSetProtectedReadonly, // error14
        public(set) private readonly string $publicSetPrivateReadonly, // error15
        public(set) readonly public string $publicSetReadonlyPublic,
        public(set) readonly protected string $publicSetReadonlyProtected, // error16
        public(set) readonly private string $publicSetReadonlyPrivate, // error17
        protected(set) public readonly string $protectedSetPublicReadonly,
        protected(set) protected readonly string $protectedSetProtectedReadonly,
        protected(set) private readonly string $protectedSetPrivateReadonly, // error18
        protected(set) readonly public string $protectedSetReadonlyPublic,
        protected(set) readonly protected string $protectedSetReadonlyProtected,
        protected(set) readonly private string $protectedSetReadonlyPrivate, // error19
        private(set) public readonly string $privateSetPublicReadonly,
        private(set) protected readonly string $privateSetProtectedReadonly,
        private(set) private readonly string $privateSetPrivateReadonly,
        private(set) readonly public string $privateSetReadonlyPublic,
        private(set) readonly protected string $privateSetReadonlyProtected,
        private(set) readonly private string $privateSetReadonlyPrivate,
        readonly public public(set) string $readOnlyPublicPublicSet,
        readonly public protected(set) string $readOnlyPublicProtectedSet,
        readonly public private(set) string $readOnlyPublicPrivateSet,
        readonly protected public(set) string $readOnlyProtectedPublicSet, // error20
        readonly protected protected(set) string $readOnlyProtectedProtectedSet,
        readonly protected private(set) string $readOnlyProtectedPrivateSet,
        readonly private public(set) string $readOnlyPrivatePublicSet, // error21
        readonly private protected(set) string $readOnlyPrivateProtectedSet, // error22
        readonly private private(set) string $readOnlyPrivatePrivateSet,
        readonly public(set) public string $readOnlyPublicSetPublic,
        readonly public(set) protected string $readOnlyPublicSetProtected, // error23
        readonly public(set) private string $readOnlyPublicSetPrivate, // error24
        readonly protected(set) public string $readOnlyProtectedSetPublic,
        readonly protected(set) protected string $readOnlyProtectedSetProtected,
        readonly protected(set) private string $readOnlyProtectedSetPrivate, // error25
        readonly private(set) public string $readOnlyPrivateSetPublic,
        readonly private(set) protected string $readOnlyPrivateSetProtected,
        readonly private(set) private string $readOnlyPrivateSetPrivate = "test",
    ) {
    }
};
