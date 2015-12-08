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
package me.st28.flexseries.flexchat.api;

import me.st28.flexseries.flexchat.api.channel.Channel;
import me.st28.flexseries.flexchat.api.chatter.Chatter;

public abstract class ChatVariable {

    private String variable;

    public ChatVariable(String variable) {
        this.variable = variable;
    }

    public String getVariable() {
        return variable;
    }

    public String getReplaceKey() {
        return "{" + variable + "}";
    }

    /**
     * Returns the replacement string for a particular chatter in a channel.
     *
     * @return The replacement string that will be used instead of the variable.<br />
     *         Null if there is no replacement.
     */
    public abstract String getReplacement(Chatter chatter, Channel channel);

}