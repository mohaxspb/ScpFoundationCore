package ru.kuchanov.scpcore.mvp.contract.tags;

import java.util.List;

import ru.kuchanov.scpcore.db.model.Article;
import ru.kuchanov.scpcore.db.model.ArticleTag;
import ru.kuchanov.scpcore.mvp.base.BaseMvp;
import ru.kuchanov.scpcore.mvp.contract.DrawerMvp;

/**
 * Created by y.kuchanov on 21.12.16.
 * <p>
 * for scp_ru
 */
public interface TagsSearchMvp extends DrawerMvp {

    interface View extends BaseMvp.View {

        void showAllTags(List<ArticleTag> data);

        void showSwipeProgress(final boolean show);

        void enableSwipeRefresh(boolean enable);

        void showProgress(boolean show);

        void showSearchResults(List<Article> data);
    }

    interface Presenter extends BaseMvp.Presenter<View> {

        void getTagsFromApi();

        void getTagsFromDb();

        List<ArticleTag> getTags();

        void searchByTags(List<ArticleTag> tags);
    }
}