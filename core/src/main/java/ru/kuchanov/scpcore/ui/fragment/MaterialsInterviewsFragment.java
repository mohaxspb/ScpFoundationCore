package ru.kuchanov.scpcore.ui.fragment;

import ru.kuchanov.scpcore.BaseApplication;
import ru.kuchanov.scpcore.R;
import ru.kuchanov.scpcore.mvp.contract.MaterialsInterviewsMvp;

/**
 * Created by mohax on 03.01.2017.
 * <p>
 * for scp_ru
 */
public class MaterialsInterviewsFragment
        extends BaseListArticlesWithSearchFragment<MaterialsInterviewsMvp.View, MaterialsInterviewsMvp.Presenter>
        implements MaterialsInterviewsMvp.View {

    public static final String TAG = MaterialsInterviewsFragment.class.getSimpleName();

    public static MaterialsInterviewsFragment newInstance() {
        return new MaterialsInterviewsFragment();
    }

    @Override
    protected void callInjections() {
        BaseApplication.getAppComponent().inject(this);
    }

    @Override
    protected void initViews() {
        super.initViews();

        if (getUserVisibleHint()) {
            if (getActivity() instanceof ArticleFragment.ToolbarStateSetter) {
                ((ArticleFragment.ToolbarStateSetter) getActivity()).setTitle(getString(R.string.materials_interview));
            }
        }
    }

    @Override
    public void resetOnScrollListener() {
        //we do not have paging
    }

    @Override
    protected boolean shouldUpdateThisListOnLaunch() {
        return false;
    }
}