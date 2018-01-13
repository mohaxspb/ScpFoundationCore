package ru.kuchanov.scpcore.ui.fragment.materials;

import ru.kuchanov.scpcore.BaseApplication;
import ru.kuchanov.scpcore.R;
import ru.kuchanov.scpcore.mvp.contract.materials.MaterialsArchiveMvp;
import ru.kuchanov.scpcore.ui.fragment.article.ArticleFragment;
import ru.kuchanov.scpcore.ui.fragment.articleslists.BaseListArticlesWithSearchFragment;

/**
 * Created by mohax on 03.01.2017.
 * <p>
 * for scp_ru
 */
public class MaterialsArchiveFragment
        extends BaseListArticlesWithSearchFragment<MaterialsArchiveMvp.View, MaterialsArchiveMvp.Presenter>
        implements MaterialsArchiveMvp.View {

    public static final String TAG = MaterialsArchiveFragment.class.getSimpleName();

    public static MaterialsArchiveFragment newInstance() {
        return new MaterialsArchiveFragment();
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
                ((ArticleFragment.ToolbarStateSetter) getActivity()).setTitle(getString(R.string.materials_archive));
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