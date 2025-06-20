package com.github.MrAn0nym;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle; 
import android.view.View; 
import android.view.ViewGroup; 
import android.widget.ImageView; 
import android.widget.LinearLayout; 
import android.widget.TextView;

import com.aliucord.Utils; 
import com.aliucord.annotations.AliucordPlugin;
import com.aliucord.api.SettingsAPI;
import com.aliucord.entities.Plugin; 
import com.aliucord.widgets.BottomSheet; 
import com.discord.models.domain.ModelGuildFolder;
import com.discord.stores.StoreStream; 
import com.discord.utilities.icon.IconUtils;

import java.util.HashSet; import java.util.List;
import java.util.Set;

@AliucordPlugin 
@SuppressWarnings("unused") 
public class BetterFolders extends Plugin {

public static final String ENABLE_SIDEBAR = "EnableFolderSidebar";
public static final String ENABLE_ANIM = "EnableFolderAnim";
public static final String CLOSE_OTHERS = "CloseOtherFolders";

private final Set<Long> expandedFolders = new HashSet<>();
private LinearLayout folderSidebar;

public BetterFolders() {
    settingsTab = new SettingsTab(BetterFoldersSettings.class, SettingsTab.Type.BOTTOM_SHEET).withArgs(settings);
}

@Override
public void start(Context context) {
    Utils.mainThread.post(() -> buildSidebar(context));
}

private void buildSidebar(Context context) {
    folderSidebar = new LinearLayout(context);
    folderSidebar.setOrientation(LinearLayout.VERTICAL);
    folderSidebar.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
    folderSidebar.setBackgroundColor(Color.parseColor("#1e1e2f"));
    folderSidebar.setPadding(16, 16, 16, 16);

    List<ModelGuildFolder> folders = StoreStream.getGuildFolders().getFolders();
    for (ModelGuildFolder folder : folders) {
        long folderId = folder.getFolderId();
        String folderName = folder.getName() != null ? folder.getName() : "Unnamed Folder";

        LinearLayout item = new LinearLayout(context);
        item.setOrientation(LinearLayout.HORIZONTAL);
        item.setPadding(8, 8, 8, 8);
        item.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        ImageView icon = new ImageView(context);
        icon.setImageDrawable(IconUtils.getForFolder(folder, 64));
        icon.setLayoutParams(new LinearLayout.LayoutParams(64, 64));

        TextView label = new TextView(context);
        label.setText(folderName);
        label.setTextColor(Color.WHITE);
        label.setPadding(16, 0, 0, 0);

        item.addView(icon);
        item.addView(label);

        item.setOnClickListener(v -> {
            if (expandedFolders.contains(folderId)) {
                expandedFolders.remove(folderId);
            } else {
                if (settings.getBool(CLOSE_OTHERS, false)) {
                    expandedFolders.clear();
                }
                expandedFolders.add(folderId);
            }
            Utils.showToast("Toggled folder: " + folderName);
        });

        folderSidebar.addView(item);
    }

    Utils.showToast("BetterFolders sidebar built.");
}

@Override
public void stop(Context context) {
    expandedFolders.clear();
}

public static class BetterFoldersSettings extends BottomSheet {

    private final SettingsAPI settings;

    public BetterFoldersSettings(SettingsAPI settings) {
        this.settings = settings;
    }

    @Override
    public void onViewCreated(View view, Bundle bundle) {
        super.onViewCreated(view, bundle);
        LinearLayout layout = getLinearLayout();

        layout.addView(createSwitch("Enable Folder Sidebar", ENABLE_SIDEBAR, true));
        layout.addView(createSwitch("Enable Folder Animation", ENABLE_ANIM, true));
        layout.addView(createSwitch("Close other folders on open", CLOSE_OTHERS, false));
    }

    private View createSwitch(String title, String key, boolean def) {
        var setting = Utils.createCheckedSetting(requireContext(), com.aliucord.views.CheckedSetting.ViewType.SWITCH, title, null);
        setting.setChecked(settings.getBool(key, def));
        setting.setOnCheckedListener(checked -> settings.setBool(key, checked));
        return setting;
    }
}

}

		
