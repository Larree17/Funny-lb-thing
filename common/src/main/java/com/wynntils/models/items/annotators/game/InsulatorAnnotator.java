/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.annotators.game;

import com.wynntils.core.text.StyledText;
import com.wynntils.models.gear.type.GearTier;
import com.wynntils.models.items.items.game.GameItem;
import com.wynntils.models.items.items.game.InsulatorItem;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.world.item.ItemStack;

public class InsulatorAnnotator extends GameItemAnnotator {
    private static final Pattern INSULATOR_PATTERN = Pattern.compile("^§(.)Corkian Insulator$");

    @Override
    public GameItem getAnnotation(ItemStack itemStack, StyledText name, List<StyledText> lore, int emeraldPrice) {
        Matcher matcher = name.getMatcher(INSULATOR_PATTERN);
        if (!matcher.matches()) return null;

        char colorChar = matcher.group(1).charAt(0);
        GearTier gearTier = GearTier.fromChatFormatting(ChatFormatting.getByCode(colorChar));

        if (gearTier == null) return null;

        return new InsulatorItem(emeraldPrice, gearTier);
    }
}