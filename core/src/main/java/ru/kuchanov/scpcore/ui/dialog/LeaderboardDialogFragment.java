package ru.kuchanov.scpcore.ui.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Locale;
import java.util.TimeZone;

import butterknife.ButterKnife;
import ru.kuchanov.scpcore.R;
import ru.kuchanov.scpcore.api.model.response.LeaderBoardResponse;
import ru.kuchanov.scpcore.ui.adapter.LeaderboardRecyclerAdapter;
import timber.log.Timber;
import xyz.danoz.recyclerviewfastscroller.vertical.VerticalRecyclerViewFastScroller;

public class LeaderboardDialogFragment extends DialogFragment {

    public static final String TAG = LeaderboardDialogFragment.class.getSimpleName();
    private static final String EXTRA_LEADERBOARD_RESPONSE = "EXTRA_LEADERBOARD_RESPONSE";

    private LeaderBoardResponse mLeaderBoardResponse;

    public static DialogFragment newInstance(LeaderBoardResponse leaderBoardResponse) {
        DialogFragment dialogFragment = new LeaderboardDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable(EXTRA_LEADERBOARD_RESPONSE, leaderBoardResponse);
        dialogFragment.setArguments(args);
        return dialogFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLeaderBoardResponse = (LeaderBoardResponse) getArguments().getSerializable(EXTRA_LEADERBOARD_RESPONSE);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Timber.d("onCreateDialog");
        MaterialDialog dialog;

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(mLeaderBoardResponse.lastUpdated);
        calendar.setTimeZone(TimeZone.getTimeZone(mLeaderBoardResponse.timeZone));
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss zzzz", Locale.getDefault());
        String refreshed = simpleDateFormat.format(calendar.getTime());

        MaterialDialog.Builder dialogTextSizeBuilder = new MaterialDialog.Builder(getActivity());
        dialogTextSizeBuilder
                .title(R.string.leaderboard_dialog_title)
                .customView(R.layout.dialog_leaderboard, false)
                .positiveText(android.R.string.cancel);

        LeaderboardRecyclerAdapter adapter = new LeaderboardRecyclerAdapter();
        Collections.sort(mLeaderBoardResponse.users, (user1, user) -> user.score - user1.score);
        adapter.setData(mLeaderBoardResponse.users);

        dialog = dialogTextSizeBuilder.build();

        TextView content = (TextView) dialog.findViewById(R.id.content);
        content.setText(getString(R.string.refreshed, refreshed));

        RecyclerView recyclerView = (RecyclerView) dialog.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(adapter);

        VerticalRecyclerViewFastScroller mVerticalRecyclerViewFastScroller = (VerticalRecyclerViewFastScroller) dialog.findViewById(R.id.fastScroller);
        mVerticalRecyclerViewFastScroller.setRecyclerView(recyclerView);

        // Connect the scroller to the recycler (to let the recycler scroll the scroller's handle)
        recyclerView.addOnScrollListener(mVerticalRecyclerViewFastScroller.getOnScrollListener());

        return dialog;
    }
}