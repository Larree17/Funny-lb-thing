/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.spells;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Model;
import com.wynntils.mc.event.SubtitleSetTextEvent;
import com.wynntils.models.spells.event.SpellEvent;
import com.wynntils.models.spells.event.SpellProgressEvent;
import com.wynntils.models.spells.event.SpellSegmentUpdateEvent;
import com.wynntils.models.spells.event.TrySpellCastEvent;
import com.wynntils.models.spells.type.SpellDirection;
import com.wynntils.models.spells.type.SpellType;
import java.util.Arrays;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class SpellModel extends Model {
    // If you modify please test with link below
    // If you pass the tests and it still doesn't work, please resync tests with the game and update the link here
    // https://regexr.com/76ijo
    private static final Pattern SPELL_TITLE_PATTERN = Pattern.compile(
            "§a([LR]|Right|Left)§7-§[a7](?:§n)?([LR?]|Right|Left)§7-§r§[a7](?:§n)?([LR?]|Right|Left)§r");

    private SpellDirection[] lastSpell = SpellDirection.NO_SPELL;

    @SubscribeEvent
    public void onSpellSegmentUpdate(SpellSegmentUpdateEvent e) {
        Matcher matcher = e.getMatcher();
        if (!matcher.matches()) return;

        SpellDirection[] spell = getSpellFromMatcher(e.getMatcher());
        if (Arrays.equals(spell, lastSpell)) return; // Wynn sometimes sends duplicate packets, skip those
        lastSpell = spell;

        WynntilsMod.postEvent(new SpellProgressEvent(spell, SpellEvent.Source.HOTBAR));

        if (!matcher.group(3).equals("?")) {
            WynntilsMod.postEvent(
                    new TrySpellCastEvent(spell, SpellEvent.Source.HOTBAR, SpellType.fromSpellDirectionArray(spell)));
        }
    }

    @SubscribeEvent
    public void onSubtitleSetText(SubtitleSetTextEvent e) {
        Matcher matcher = SPELL_TITLE_PATTERN.matcher(e.getComponent().getString());
        if (!matcher.matches()) return;

        SpellDirection[] spell = getSpellFromMatcher(matcher);
        if (Arrays.equals(spell, lastSpell)) return; // Wynn sometimes sends duplicate packets, skip those
        lastSpell = spell;

        // This check looks for the "t" in Right and Left, that do not exist in L and R, to set the source
        SpellEvent.Source source =
                (matcher.group(1).endsWith("t")) ? SpellEvent.Source.TITLE_FULL : SpellEvent.Source.TITLE_LETTER;

        WynntilsMod.postEvent(new SpellProgressEvent(spell, source));

        if (!matcher.group(3).equals("?")) {
            WynntilsMod.postEvent(new TrySpellCastEvent(spell, source, SpellType.fromSpellDirectionArray(spell)));
        }
    }

    private static SpellDirection[] getSpellFromMatcher(MatchResult spellMatcher) {
        int size = 1;
        for (; size < 3; ++size) {
            if (spellMatcher.group(size + 1).equals("?")) break;
        }

        SpellDirection[] spell = new SpellDirection[size];
        for (int i = 0; i < size; ++i) {
            spell[i] = spellMatcher.group(i + 1).charAt(0) == 'R' ? SpellDirection.RIGHT : SpellDirection.LEFT;
        }

        return spell;
    }
}