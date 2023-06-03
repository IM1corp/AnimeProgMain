package com.imcorp.animeprog.MainActivity.fragments.Adapters;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import com.imcorp.animeprog.Config;

import static java.util.Objects.requireNonNull;

public class Separator extends DividerItemDecoration {
    private final Context mContext;
    private final int pix118;
    public Separator(Context context, Drawable drawable) {
        super(context, DividerItemDecoration.VERTICAL);
        this.mContext = context;
        this.setDrawable(drawable);
        this.pix118 = Config.dpToPix(mContext,118);
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        final int pos = parent.getChildAdapterPosition(view);
        if (pos == 0) {
            return;
        }
        outRect.top = requireNonNull(getDrawable()).getIntrinsicHeight();
    }
    @Override
    public void onDraw(Canvas canvas, RecyclerView parent, RecyclerView.State state) {
        int dividerLeft = parent.getPaddingLeft() + pix118;
        int dividerRight = parent.getWidth() - parent.getPaddingRight();

        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount - 1; i++) {
            View child = parent.getChildAt(i);

            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();

            int dividerTop = child.getBottom() + params.bottomMargin;
            int dividerBottom = dividerTop + requireNonNull(getDrawable()).getIntrinsicHeight();

            getDrawable().setBounds(dividerLeft, dividerTop, dividerRight, dividerBottom);
            getDrawable().draw(canvas);
        }
    }
}
