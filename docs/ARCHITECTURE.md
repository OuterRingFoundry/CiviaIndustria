# Architecture

The entry point wires empty deferred registers, configuration specs and command registration.
Command output queries installed versions rather than duplicating a mod version constant.

api/ owns future integration contracts. common/ owns server/domain systems.
Minecraft-independent domain logic should remain behind narrow platform adapters.
compat/create, compat/immersiveengineering and compat/kubejs are reserved,
isolated packages; none currently load or reference optional mods.
client/ is reserved exclusively for client implementations. Common code imports
no net.minecraft.client classes. ClientConfig contains only the side-safe config
spec; NeoForge manages its CLIENT scope.

Package-info files reserve the requested structure without implementing gameplay.
No components, tabs or placeholder blocks are registered.
