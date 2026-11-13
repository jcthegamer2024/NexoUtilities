# **NexoUtilities**
NexoUtilities is an unofficial Nexo Addon that adds two features as of right now, but open to suggestions! Here are the current features:

- Directional CustomBlock mechanic for STRINGBLOCK and CHORUSBLOCK types, with identical config syntax as the regular NOTEBLOCK nexo directional configurations. 
  Reads the allocations for all types of custom blocks currently in Nexo.![Nexo CustomBlock Utilisation ](https://cdn.modrinth.com/data/cached_images/28b119cb52e1548238c89dea57747fe1fffe7227.png)
- A set of commands to view the current live usage of the Nexo customBlock types.

## Directional CustomBlock for other block types
This is an optional module toggleable in _config.yml_ which adds directional placement to CHORUSBLOCK and STRINGBLOCK customBlock types. Uses the familar Nexo directional configuration. Supports LOG, BARREL, FURNACE, and DROPPER modes, with automatic model rotation or existing directional variants. Refer to Nexo's Directional Mechanic documentation for how to lay out the config.

## CustomBlock Allocation Checker
set of commands that reads allocation data directly from Nexo's live custom block registries, showing how many NOTEBLOCK, CHORUSBLOCK, and STRINGBLOCK variations are in use, alongside Nexo's supported maximum amount for each of the variants. The count of this data refreshes whenever you run the command again. This data includes the registered directional variants within the count. It counts allocated custom block states, rather than the blocks placed in the world. 

### Commands
```
/nexoutils allblocks 
```
Reads the allocations for all types of custom blocks currently in Nexo.

```
/nexoutils noteblocks
```
Reads the allocations for noteblock custom blocks currently in Nexo.

```
/nexoutils chorusblocks
```
Reads the allocations for chorusblock custom blocks currently in Nexo.

```
/nexoutils stringblocks
```
Reads the allocations for stringblock custom blocks currently in Nexo.

If you encounter any bugs or would like to suggest new features feel free to add them in the issues tab of the github source! 
