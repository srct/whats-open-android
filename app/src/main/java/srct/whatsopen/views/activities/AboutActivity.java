package srct.whatsopen.views.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.danielstone.materialaboutlibrary.MaterialAboutActivity;
import com.danielstone.materialaboutlibrary.model.MaterialAboutActionItem;
import com.danielstone.materialaboutlibrary.model.MaterialAboutCard;
import com.danielstone.materialaboutlibrary.model.MaterialAboutList;
import com.danielstone.materialaboutlibrary.model.MaterialAboutTitleItem;

import srct.whatsopen.MyApplication;
import srct.whatsopen.R;

public class AboutActivity extends MaterialAboutActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        MyApplication.setRotation(this);
    }

    @Override
    protected MaterialAboutList getMaterialAboutList() {
        MaterialAboutCard.Builder appCardBuilder = new MaterialAboutCard.Builder();

        appCardBuilder.addItem(new MaterialAboutTitleItem.Builder()
                .text("What's Open")
                .icon(R.drawable.ic_wo_icon)
                .build());
        appCardBuilder.addItem(new MaterialAboutActionItem.Builder()
                .text("Version")
                .subText("1.0 \"Cassowary Edition\"")
                .icon(R.drawable.ic_info_outline_black_24dp)
                .build());

        MaterialAboutCard.Builder contributorsCardBuilder = new MaterialAboutCard.Builder();
        contributorsCardBuilder.title("Contributors");
        contributorsCardBuilder.addItem(new MaterialAboutActionItem.Builder()
                .text("Version 1.0")
                .icon(R.drawable.ic_people_black_24dp)
                .subText("Robert Hitt, Tanner Grehawick, Jason Yeomans")
                .build());

        MaterialAboutCard.Builder aboutCardBuilder = new MaterialAboutCard.Builder();
        aboutCardBuilder.title("About");
        aboutCardBuilder.addItem(new MaterialAboutActionItem.Builder()
                .text("What's Open is a GMU SRCT project")
                .icon(R.drawable.ic_srct_logo)
                .subText("srct.gmu.edu")
                .setOnClickListener(new MaterialAboutActionItem.OnClickListener() {
                    @Override
                    public void onClick() {
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setData(Uri.parse("https://srct.gmu.edu/"));
                        startActivity(i);
                    }
                })
                .build());
        aboutCardBuilder.addItem(new MaterialAboutActionItem.Builder()
                .text("Project Repository")
                .icon(R.drawable.ic_gitlab_logo)
                .subText("git.gmu.edu/srct/whats-open-android")
                .setOnClickListener(new MaterialAboutActionItem.OnClickListener() {
                    @Override
                    public void onClick() {
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setData(Uri.parse("https://git.gmu.edu/srct/whats-open-android"));
                        startActivity(i);
                    }
                })
                .build());

        return new MaterialAboutList(appCardBuilder.build(), contributorsCardBuilder.build(),
                aboutCardBuilder.build());
    }

    @Override
    protected CharSequence getActivityTitle() {
        return "About";
    }
}
