# FlexChat

## Goals

- Channel-based chat system for Minecraft
- Provide a flexible and easy to use API for in-game chat and external chat (Discord, IRC, etc.)

## Terminology

- **Channel**: A filtered view of what would otherwise be global chat.
- **Channel Instance**: An instance of a channel. Most channels are simple, having only one instance. However, complex channels could exist where players are grouped into different instances based on various requirements (ex. Towny town, mcMMO party, etc.)
- **Chat Provider**: Something that provides chat messages.

## Functionality Overview

- Channels have no functionality on their own. They simply provide information (name, description, chat formatting, etc.). The real work in handling chat messages happens in instances of channels.
- Chat providers are registered with FlexChat and provide raw message information to a channel:
    - **sender** (required): The name of the entity that sent the message
    - **message** (required): The message string
    - *Other provider-specific custom data*
    
    
# Chat Providers

Configuration is always located in `config-chat.yml`
