package ru.kuchanov.scpcore.ui.fragment.materials;

import ru.kuchanov.scpcore.BaseApplication;
import ru.kuchanov.scpcore.R;
import ru.kuchanov.scpcore.mvp.contract.materials.MaterialsIncidentsMvp;
import ru.kuchanov.scpcore.ui.fragment.article.ArticleFragment;
import ru.kuchanov.scpcore.ui.fragment.articleslists.BaseListArticlesWithSearchFragment;

/**
 * Created by mohax on 03.01.2017.
 * <p>
 * for scp_ru
 */
public class MaterialsIncidentsFragment
        extends BaseListArticlesWithSearchFragment<MaterialsIncidentsMvp.View, MaterialsIncidentsMvp.Presenter>
        implements MaterialsIncidentsMvp.View {

    public static final String TAG = MaterialsIncidentsFragment.class.getSimpleName();

    public static MaterialsIncidentsFragment newInstance() {
        return new MaterialsIncidentsFragment();
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
                ((ArticleFragment.ToolbarStateSetter) getActivity()).setTitle(getString(R.string.materials_incidents));
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