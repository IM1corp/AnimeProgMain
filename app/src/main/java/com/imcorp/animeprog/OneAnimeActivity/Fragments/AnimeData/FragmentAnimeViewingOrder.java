package com.imcorp.animeprog.OneAnimeActivity.Fragments.AnimeData;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.imcorp.animeprog.OneAnimeActivity.OneAnimeActivity;
import com.imcorp.animeprog.R;
import com.imcorp.animeprog.Requests.JsonObj.OneAnime;

public class FragmentAnimeViewingOrder extends RecyclerView.ViewHolder  {
    //private TextView num,title,description;
    private OneAnimeActivity context;
    private TextView numTextView,textView;
    private OneAnime currentAnime;
    public FragmentAnimeViewingOrder(LinearLayout view, OneAnimeActivity context) {
        super(view);
        this.context=context;
        this.numTextView = new TextView(view.getContext());
        this.textView = new TextView(view.getContext());

        this.textView.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        this.textView.setSingleLine(true);
        this.textView.setHorizontallyScrolling(true);
        this.textView.setMarqueeRepeatLimit(-1);
        this.textView.setSelected(true);

        view.addView(numTextView);
        view.addView(textView);

    }
    @SuppressLint("SetTextI18n")
    public void bind(OneAnime anime, int number, @Nullable OnAnimeClick onClick) {
        this.currentAnime =anime;
        SpannableString span = new SpannableString(number+".");
        span.setSpan(new StyleSpan(Typeface.BOLD),0,span.length()-1,0);
        this.numTextView.setText(span);
        this.textView.setText(getSpannableText(onClick));
    }
    private Spannable getSpannableText(@Nullable OnAnimeClick onClick){
        SpannableStringBuilder builder = new SpannableStringBuilder();
        SpannableString title = new SpannableString(currentAnime.title);
        if(currentAnime.equals(context.getOneAnime())){
            title.setSpan(new ForegroundColorSpan(Color.BLACK),0,title.length(),0);
            title.setSpan(new StyleSpan(Typeface.BOLD),0,title.length(),0);
        }
        else if(currentAnime.getPath()==null || currentAnime.getPath().isEmpty());
        else {
            title.setSpan(new UnderlineSpan(),0,title.length(),0);
            title.setSpan(new StyleSpan(Typeface.BOLD),0,title.length(),0);
            title.setSpan(new ForegroundColorSpan(context.getResources().getColor(R.color.link_color)),0,title.length(),0);
            this.setOnclick(onClick);
        }
        builder.append(title)
            .append(" - ")
            .append(currentAnime.description);


        SpannableString year = new SpannableString(String.valueOf(currentAnime.year));
        year.setSpan(new StyleSpan(Typeface.BOLD),0,year.length(),0);

        builder.append(" ( ")
                .append(year)
                .append(" )");
        return builder;
    }
    private void setOnclick(@Nullable final OnAnimeClick onClick){
        this.textView.setOnClickListener(view -> {
            if(!currentAnime.equals(context.getOneAnime())&&onClick!=null){
                onClick.onClick(currentAnime);
            } });
    }
    public interface OnAnimeClick {
        public void onClick(OneAnime anime);
    }

}
