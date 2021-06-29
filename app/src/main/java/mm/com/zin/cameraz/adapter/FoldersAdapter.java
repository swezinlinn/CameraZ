package mm.com.zin.cameraz.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.Map;

import mm.com.zin.cameraz.GalleryActivity;
import mm.com.zin.cameraz.R;
import mm.com.zin.cameraz.utils.Image;

public class FoldersAdapter extends RecyclerView.Adapter<FoldersAdapter.ViewHolder> {

    private Context context;
    private Map<String, ArrayList<Image>> folders;
    private ArrayList<String> folderNames = new ArrayList<>();

    public FoldersAdapter(Context context, Map<String, ArrayList<Image>> folders) {
        this.context = context;
        this.folders = folders;
        folderNames.addAll(folders.keySet());
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView folderImage;
        TextView folderName;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            folderImage = itemView.findViewById(R.id.folderImage);
            folderName = itemView.findViewById(R.id.folderName);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_folder, null);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String placeHolderText = folderNames.get(position)+" ("+folders.get(folderNames.get(position)).size()+")";
        holder.folderName.setText(placeHolderText);
        String img = folders.get(folderNames.get(position)).get(0).getImgUri();
        Glide.with(context)
                .load(img)
                .error(R.drawable.error_image)
                .into(holder.folderImage);
        holder.folderImage.setOnClickListener(view -> {
            Intent intent = new Intent(context.getApplicationContext(), GalleryActivity.class);
            intent.putParcelableArrayListExtra("images", folders.get(folderNames.get(position)));
            intent.putExtra("folder_name", folderNames.get(position));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        });
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return folders.size();
    }

}
