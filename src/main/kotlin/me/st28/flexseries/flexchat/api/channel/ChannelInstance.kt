/**
 * Copyright 2016 Stealth2800 <http://stealthyone.com/>
 * Copyright 2016 Contributors <https://github.com/FlexSeries>
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
package me.st28.flexseries.flexchat.api.channel

import me.st28.flexseries.flexchat.api.chatter.Chatter
import java.util.*

/**
 * Represents an instance of a [Channel].
 *
 * @param name The name of the instance.
 */
class ChannelInstance(val channel: Channel, val name: String) {

    /**
     * The chatters in this channel instance.
     */
    internal val chatters: MutableSet<Chatter> = HashSet()

    /**
     * @return True if this instance contains the specified [Chatter].
     */
    fun containsChatter(chatter: Chatter): Boolean {
        return chatters.contains(chatter)
    }

    fun addChatter(chatter: Chatter, silent: Boolean): JoinResult {
        return chatter.addInstance(this, silent)
    }

    fun addChatterUnsafe(chatter: Chatter, silent: Boolean): Boolean {
        return chatter.addInstanceUnsafe(this, silent)
    }

    enum class JoinResult {

        SUCCESS,

        NO_PERMISSION,

        ALREADY_JOINED

    }

}
