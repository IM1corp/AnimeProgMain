package com.imcorp.animeprog.OneAnimeActivity.Fragments.AnimeData;

import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.imcorp.animeprog.OneAnimeActivity.OneAnimeActivity;
import com.imcorp.animeprog.Requests.JsonObj.OneAnime;

import java.util.ArrayList;

public class FragmentAnimeViewingOrderAdapter extends RecyclerView.Adapter<FragmentAnimeViewingOrder> {
    private final FragmentAnimeViewingOrder.OnAnimeClick onAnimeClick;
    private final OneAnimeActivity context;
    public ArrayList<OneAnime> animes;
    public FragmentAnimeViewingOrderAdapter(OneAnimeActivity context, ArrayList<OneAnime> animes, FragmentAnimeViewingOrder.OnAnimeClick onAnimeClick){
        this.context=context;
        this.animes=animes;
        this.onAnimeClick = onAnimeClick;
    }
    @NonNull
    @Override
    public FragmentAnimeViewingOrder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout view = new LinearLayout(context);
        view.setOrientation(LinearLayout.HORIZONTAL);
        //view = inflater.inflate(R.layout.fragment_one_viewing_order, null, false);
        return new FragmentAnimeViewingOrder(view,context);
    }

    @Override
    public void onBindViewHolder(@NonNull FragmentAnimeViewingOrder holder, int position) {
        holder.bind(animes.get(position),position+1,onAnimeClick);
    }

    @Override
    public int getItemCount() {
        return animes.size();
    }

}
