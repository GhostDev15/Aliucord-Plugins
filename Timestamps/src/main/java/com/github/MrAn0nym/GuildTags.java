package com.github.MrAn0nym;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.aliucord.Utils;
import com.aliucord.annotations.AliucordPlugin;
import com.aliucord.api.SettingsAPI;
import com.aliucord.entities.Plugin;
import com.aliucord.patcher.Hook;
import com.aliucord.patcher.PinePatchFn;
import com.aliucord.widgets.BottomSheet;
import com.discord.models.domain.ModelGuild;
import com.discord.stores.StoreStream;
import com.discord.widgets.guilds.list.WidgetGuildItem;

import java.lang.reflect.Field;

@AliucordPlugin
@SuppressWarnings("unused")
public class GuildTags extends Plugin {

    public GuildTags() {
        settingsTab = new SettingsTab(GuildTagsSettings.class, SettingsTab.Type.BOTTOM_SHEET).withArgs(settings);
    }

    @Override
    public void start(Context ctx) {
        patcher.patch(
            WidgetGuildItem.class.getDeclaredMethod("onConfigure", int.class, Object.class),
            new PinePatchFn(callFrame -> {
                WidgetGuildItem widget = (WidgetGuildItem) callFrame.thisObject;
                Object item = callFrame.args[1];
                if (!(item instanceof ModelGuild)) return;

                ModelGuild guild = (ModelGuild) item;
                long guildId = guild.getId();
                String tag = settings.getString("tag_" + guildId, null);
                if (tag == null || tag.isEmpty()) return;

                try {
                    Field itemViewField = WidgetGuildItem.class.getDeclaredField("itemView");
                    itemViewField.setAccessible(true);
                    View itemView = (View) itemViewField.get(widget);

                    // Check if tag is already added
                    if (itemView.findViewWithTag("guild_tag_" + guildId) != null) return;

                    TextView tagView = new TextView(itemView.getContext());
                    tagView.setTag("guild_tag_" + guildId);
                    tagView.setText(tag);
                    tagView.setTextSize(10f);
                    tagView.setTextColor(Color.WHITE);
                    tagView.setBackgroundColor(Color.parseColor("#99000000")); // semi-transparent black
                    tagView.setPadding(6, 2, 6, 2);

                    FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.WRAP_CONTENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT
                    );
                    params.leftMargin = 90;
                    params.topMargin = 10;
                    tagView.setLayoutParams(params);

                    if (itemView instanceof FrameLayout) {
                        ((FrameLayout) itemView).addView(tagView);
                    } else if (itemView instanceof LinearLayout) {
                        ((LinearLayout) itemView).addView(tagView);
                    }
                } catch (Exception e) {
                    logger.error("Failed to attach guild tag", e);
                }
            })
        );
    }

    @Override
    public void stop(Context ctx) {
        patcher.unpatchAll();
    }

    public static class GuildTagsSettings extends BottomSheet {

        private final SettingsAPI settings;

        public GuildTagsSettings(SettingsAPI settings) {
            this.settings = settings;
        }

        @Override
        public void onViewCreated(View view, Bundle bundle) {
            super.onViewCreated(view, bundle);
            LinearLayout layout = getLinearLayout();

            layout.addView(Utils.createTextElement(getContext(), "Set a tag for each guild:"));

            for (ModelGuild guild : StoreStream.getGuilds().getGuilds().values()) {
                String guildName = guild.getName();
                long guildId = guild.getId();

                var input = Utils.createTextInput(getContext());
                input.setHint("Tag for " + guildName);
                input.setText(settings.getString("tag_" + guildId, ""));
                input.setOnTextChanged(str -> settings.setString("tag_" + guildId, str));
                layout.addView(input);
            }

            layout.addView(Utils.createTextElement(getContext(), "Switch servers to refresh tags."));
        }
    }
}
