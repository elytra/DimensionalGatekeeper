    # Dimensional Gatekeeper is a rules engine mod.
    # See documentation for the .rules format here: https://github.com/elytra/Concrete/wiki/.rules-file-End-User-Documentation

    # Predicates: entity resource names, see https://minecraft.gamepedia.com/Java_Edition_data_values/Entity_IDs
    # Variables: to/from (dim ids), x/y/z, light
    # For dimension ids see https://ftbwiki.org/Dimension
    # Effects: y, n, k
    # y: allow travel, n: deny travel, k: kill on sight

    # All effects use the same slot, so note that n can mask a lower priority k.  It is recommended to define kill rules
    # narrowly and give them high rule priority.

    # n and k will both simply block teleportation attempts without any killing.
    # additionally, k rules matching an item with from=to will kill an entity if it spawns through means other than cross-dimensional travel.
    # In such a case, to and from will be the same dimension id.

    # Silly example rule to require players to light up nether portals to return to the overworld:
    # 0 (from=-1) player (light<12) (to=0) -> n
    # No dropped items allowed in the nether ever:
    # 5 (from=-1) item -> k
    # And of course, the reason this mod was made, keep some goofy modded mod in its goofy modded dimension:
    # (replace 99 with dimension id or tag for ids, and somemod:dragon with your mob or tag for mobs)
    # 5 (from!=99) somemod:dragon -> k

    # These are just usage examples.  There is no sensible default behavior.  This mod is intentionally left blank.
