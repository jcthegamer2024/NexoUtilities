# NexoUtilities

Paper addon reporting custom block allocations for players and the console:

- `/nexoutils noteblocks` — note block slots (`nexoutils.command.noteblocks`).
- `/nexoutils chorusblocks` — chorus block slots (`nexoutils.command.chorusblocks`).
- `/nexoutils stringblocks` — string/tripwire block slots (`nexoutils.command.stringblocks`).

All three permissions default to operators. Each report requires its own permission.
Targets Paper 26.3 with backward compatibility down to Paper 1.21.10.
Requires Nexo 1.30; compiled against the Nexo API artifact `1.30.2` and the Paper 1.21.10 API with
Java 21 bytecode. Run Paper 1.21.10 on Java 21 or Paper 26.3 on Java 25,
with a Nexo release compatible with your server. The same addon jar is used
for both versions. Live server compatibility still needs smoke testing.

The command reads Nexo's live `BLOCK_PER_VARIATION` registry and
`MAX_BLOCK_VARIATION` on each invocation. It counts distinct allocated variations,
including generated directional variants, rather than item definitions or placed
blocks. Variation zero is excluded because Nexo reserves it. The denominator is
Nexo's actual supported custom variation limit, rather than a hardcoded 1,000.
For chorus and string blocks, capacity comes from the live API's supported range;
only distinct registry keys within that range count, excluding reserved zero.
Disabled or not-yet-ready mechanics produce a concise status message.
External resource packs overriding states without registering them with Nexo are
outside this report.

Output includes a 40-segment bar, exact used/total/free counts, and a percentage.
Used segments are green below 75%, yellow from 75%, and red from 90%; free
segments are dark grey. Tab completion suggests the block types the sender has
permission to view.

Build with `./gradlew build`, then copy `build/libs/NexoUtilities-1.0.jar`
into the server's `plugins` folder alongside Nexo and restart. This uses
`paper-plugin.yml` with a required Nexo dependency and Paper lifecycle command
registration.

## Optional directional chorus/string blocks

Enable the module in `plugins/NexoUtilities/config.yml`:

```yaml
modules:
  directional-blocks:
    enabled: true
```

Restart the server after changing this toggle, then run `/nexo reload all` to
load the item configurations and rebuild the resource pack. Players must use
the rebuilt pack. The module defaults to disabled.

Use the same `directional` section as Nexo note blocks, inside your normal
Nexo chorus/string item definition. For example:

```yaml
custom_chorus_log:
  material: PAPER
  Pack:
    model: example:block/custom_log
  Mechanics:
    custom_block:
      type: CHORUSBLOCK # Or STRINGBLOCK
      directional:
        type: LOG
```

Supported modes are LOG (clicked axis), BARREL (clicked face), FURNACE
(horizontal facing towards the player), and DROPPER (also supports vertical
facing). LOG uses `x`, `y`, `z`; the others use `north`, `south`, `east`, `west`,
with `up`/`down` for BARREL and DROPPER.

Each direction optionally accepts `<direction>_variation`, `<direction>_model`,
or `<direction>_block`. For example, `east_model: example:block/east` uses that
model without automatic rotation; `east_block: another_string_block` uses an
existing item of the same block type. Otherwise the module allocates a state
and rotates the parent's model automatically. Explicit variation numbers must
be free and within Nexo's supported range. An unrotated direction can explicitly
reuse the parent's variation if its model is unchanged.

Generated states are registered through Nexo's external-item API. Their IDs
and assigned variations are saved in
`plugins/NexoUtilities/directional-variants.yml`; back up this file alongside
Nexo's data. Source item configurations are not rewritten. Each generated
direction consumes a real slot, visible in the allocation reports. A LOG can
use three extra slots, FURNACE four, and BARREL/DROPPER six; reusing existing
states reduces this. Insufficient capacity or conflicting assignments skip the
affected directional definition and log the reason. Saved variants cannot be
renumbered by editing `<direction>_variation`.

The module preserves parent drop rules and returns the parent item through
pick-block. It respects placement cancellation and leaves native note block
directional handling to Nexo. Tall, stackable, and random-placement string
blocks cannot currently be combined with this module. Automatic orientation
applies to player placement; API/WorldEdit placement is not automatically rotated.
Rotation is applied on the following server tick, after Nexo finishes placement.
Cancelled placements, changed blocks, unloaded chunks and pending placements
invalidated by a reload are left alone. A successful item reload logs the number
of prepared directional chorus/string definitions.

Disabling the module stops directional placement and new generation, while
saved states remain registered for already placed blocks. Removing a definition
also retains its generated states. These slots are not automatically reclaimed;
do not delete the saved file or parent definitions while their blocks remain in
worlds. Run `/nexo reload all` after changing item directional settings.

Configuration follows Nexo's
[directional mechanic](https://docs.nexomc.com/mechanics/custom-block-mechanics/noteblock-mechanic/directional-mechanic).

Server smoke checks:

- Run all three commands in console and in-game; compare with registered variations.
- Reload Nexo after adding/removing a block; run again to check the updated count.
- Check a directional block consumes its registered variant slots.
- Check an ordinary player cannot run each report until granted its permission,
  and granting one report does not unlock the others.
- Check chorus/string reports gracefully handle disabled mechanics.
- With the module enabled, test all four directional modes for both types with
  the rebuilt pack, including explicit models and variation assignments.
- Check cancelled/protected placements consume no items and leave no blocks.
- Check survival drops and creative pick-block return the parent item.
- Restart and reload Nexo; verify placed orientations and variation IDs persist.
- Disable the module; check old blocks retain their models and ordinary placement
  resumes. Test full registries and invalid configs log a useful error.

References: [Nexo API](https://docs.nexomc.com/community-guides/api),
[Paper command registration](https://docs.papermc.io/paper/dev/command-api/basics/registration/).
