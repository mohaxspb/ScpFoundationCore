package ru.kuchanov.scpcore.mvp.contract.tags;

import java.util.List;

import ru.kuchanov.scpcore.db.model.Article;
import ru.kuchanov.scpcore.db.model.ArticleTag;
import ru.kuchanov.scpcore.mvp.base.BaseArticlesListMvp;

/**
 * Created by y.kuchanov on 21.12.16.
 * <p>
 * for scp_ru
 */
public interface TagsSearchResultsArticlesMvp {

    interface View extends BaseArticlesListMvp.View {

    }

    interface Presenter extends BaseArticlesListMvp.Presenter<View> {

        void setQueryTags(List<ArticleTag> queryTags);

        List<ArticleTag> getQueryTags();

        void setArticlesUrls(List<String> articlesUrls);

        List<String> getArticlesUrls();
    }
}