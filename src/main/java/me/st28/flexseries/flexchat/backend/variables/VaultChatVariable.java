/**
 * Copyright 2015 Stealth2800 <http://stealthyone.com/>
 * Copyright 2015 Contributors <https://github.com/FlexSeries>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package me.st28.flexseries.flexchat.backend.variables;

import me.st28.flexseries.flexchat.api.ChatVariable;
import me.st28.flexseries.flexlib.hook.HookDisabledException;
import me.st28.flexseries.flexlib.hook.HookManager;
import me.st28.flexseries.flexlib.hook.defaults.VaultHook;
import me.st28.flexseries.flexlib.plugin.FlexPlugin;

public abstract class VaultChatVariable extends ChatVariable {

    public VaultChatVariable(String variable) {
        super(variable);
    }

    protected VaultHook getVaultHook() {
        try {
            return FlexPlugin.getGlobalModule(HookManager.class).getHook(VaultHook.class);
        } catch (HookDisabledException ex) {
            return null;
        }
    }

}