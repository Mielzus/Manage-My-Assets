package ca.unb.mobiledev.managemyassets;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private RecyclerView.Adapter assetAdapter;
    private ArrayList<Asset> assetList;

    DatabaseCallTask databaseCallTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button button = findViewById(R.id.asset_viewMap_button);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent mapIntent = new Intent(MainActivity.this, MapActivity.class);
                startActivity(mapIntent);
            }
        });

        // Initialize variables
        databaseCallTask = new DatabaseCallTask(this);
        FloatingActionButton mAddAssetFab = findViewById(R.id.assetAdd_fab);

        RecyclerView recyclerView = findViewById(R.id.asset_recycler_view);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);

        assetAdapter = new AssetAdapter();
        assetList = new ArrayList<>();

        mAddAssetFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AddAssetActivity.class);
                startActivity(intent);
            }
        });

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(assetAdapter);

        databaseCallTask = new DatabaseCallTask(MainActivity.this);
        databaseCallTask.execute(MMAConstants.DATABASE_SELECT_ASSETS, MMAConstants.ORIGIN_MAIN_ACTIVITY, null);
    }

    public void databaseCallFinished(Asset[] assets) {
        assetList = new ArrayList<>(Arrays.asList(assets));
        assetAdapter.notifyDataSetChanged();
    }

    public class AssetAdapter extends RecyclerView.Adapter<AssetAdapter.ViewHolder> {

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            TextView view = (TextView) LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.asset_layout, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder viewHolder, int position) {

            viewHolder.asset = assetList.get(position);
            if (viewHolder.asset.getImage() != null) {
                Bitmap imageBitmap = BitmapFactory.decodeFile(viewHolder.asset.getImage());
                Bitmap imageThumbnail = ThumbnailUtils.extractThumbnail(imageBitmap, 200, 200);
                Drawable drawable = new BitmapDrawable(getResources(), imageThumbnail);
                viewHolder.mAssetTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, drawable, null);
            }
            viewHolder.mAssetTextView.setText(Html.fromHtml(assetList.get(position).toString()));
            viewHolder.mAssetTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(MainActivity.this, AddAssetActivity.class);
                    intent.putExtra(MMAConstants.ASSET_OBJECT_NAME, viewHolder.asset);
                    intent.putExtra(MMAConstants.INTENT_NEW_ASSET, false);
                    startActivity(intent);
                }
            });
            viewHolder.mAssetTextView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    AlertDialog alertDialog = new AlertDialog.Builder(new ContextThemeWrapper(MainActivity.this, R.style.alertDialog))
                            .setTitle(getString(R.string.asset_delete))
                            .setMessage(getString(R.string.asset_delete_confirm))
                            .setNegativeButton(getString(R.string.input_button_no), null)
                            .setPositiveButton(getString(R.string.input_button_yes), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Asset asset = viewHolder.asset;
                                    databaseCallTask = new DatabaseCallTask(MainActivity.this);
                                    databaseCallTask.execute(MMAConstants.DATABASE_DELETE_ASSET, MMAConstants.ORIGIN_MAIN_ACTIVITY, asset);
                                }
                            })
                            .create();
                    alertDialog.show();
                    return true;
                }
            });
        }

        @Override
        public int getItemCount() {
            return assetList.size();
        }

        /*
            This class manages the  individual 'tiles' that are displayed in the RecyclerView
        */
        public class ViewHolder extends RecyclerView.ViewHolder {

            private TextView mAssetTextView;
            private Asset asset;

            public ViewHolder(TextView view) {
                super(view);
                mAssetTextView = view;
            }
        }
    }
}
