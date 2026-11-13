# NexoUtilities

Paper addon providing `/nexoutils noteblocks` for players and the console.
Targets Paper 26.3 with backward compatibility down to Paper 1.21.10.
Requires Nexo; compiled against Nexo 1.30.2 and the Paper 1.21.10 API with
Java 21 bytecode. Run Paper 1.21.10 on Java 21 or Paper 26.3 on Java 25,
with a Nexo release compatible with your server. The same addon jar is used
for both versions. Live server compatibility still needs smoke testing.

The command reads Nexo's live `BLOCK_PER_VARIATION` registry and
`MAX_BLOCK_VARIATION` on each invocation. It counts distinct allocated variations,
including generated directional variants, rather than item definitions or placed
blocks. Variation zero is excluded because Nexo reserves it. The denominator is
Nexo's actual supported custom variation limit, rather than a hardcoded 1,000.
External resource packs overriding states without registering them with Nexo are
outside this report.

Output includes a 40-segment bar, exact used/total/free counts, and a percentage.
Used segments are green below 75%, yellow from 75%, and red from 90%; free
segments are dark grey. Permission: `nexoutils.command.noteblocks` (operators by
default). Tab completion suggests `noteblocks`.

Build with `./gradlew build`, then copy `build/libs/NexoUtilities-1.0-SNAPSHOT.jar`
into the server's `plugins` folder alongside Nexo and restart. This uses
`paper-plugin.yml` with a required Nexo dependency and Paper lifecycle command
registration.

Server smoke checks:

- Run the command in console and in-game; compare with registered variations.
- Reload Nexo after adding/removing a block; run again to check the updated count.
- Check a directional block consumes its registered variant slots.
- Check an ordinary player cannot run the command until granted its permission.

References: [Nexo API](https://docs.nexomc.com/community-guides/api),
[Paper command registration](https://docs.papermc.io/paper/dev/command-api/basics/registration/).
