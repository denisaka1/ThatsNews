import React, {Component} from 'react';
import {v1} from 'uuid';
import {StarFill} from 'react-bootstrap-icons';
import {createInstance} from '../../actions/CreateInstance'
import {saveUser} from '../../actions/SaveUser'
import {connect} from 'react-redux';
import * as Const from '../../constants/Constants'

import './Blocks.css'

class Block extends Component {
    constructor(props) {
        super(props);
        this.state = {
            favorites: this.props.favorites
        };
        this.props.saveUser({
            user: this.props.user.user
        });
        if (!this.props.isFavPage) this.getFavorites();
    }

    getFavorites() {
        if (!this.props.user.isLoggedIn) return;
        const callback = _favorites => this.setState({favorites: _favorites});

        Const
            .getFavoritesForUser(this.props.user.user, callback)
            .then(_ => console.log('Articles loaded'))
    }

    getBackground() {
        // let rand = Math.floor(Math.random() * 2);
        // if (rand) { background = 'bg-light border'; } // random background for each element looks like a bug, not a feature. -e
        return 'text-white bg-dark';
    }

    handleOpenArticle(article) {
        const isLoggedIn = this.props.user.isLoggedIn
        if (isLoggedIn) {
            createInstance(
                article,
                this.props.user.user.userId,
                Const.READ_ARTICLE,
                this.props.category
            ).then(_ => window.location.assign(article.url));
        }
    }

    isArticleInFavorites(article) {
        const favorites = this.state.favorites
        return favorites.some(favArticle => favArticle.url === article.url);
    }

    handleClickFavorite(article) {
        const isLoggedIn = this.props.user.isLoggedIn
        if (isLoggedIn) {
            createInstance(
                article,
                this.props.user.user.userId,
                this.isArticleInFavorites(article) ? Const.REMOVE_FAVORITE : Const.SAVE_FAVORITE,
                this.props.category
            ).then(() => window.location.reload());
        }
    }


    render() {
        const isLoggedIn = this.props.user.isLoggedIn

        return (
            <div className='row align-items-md-stretch container-blocks'>
                {this.props.articles && this.props.articles.map((article) => {
                        return (
                            <div className='col-md-6 mb-4' key={article.url}>
                                <div className={`h-100 p-5 ${this.getBackground()} rounded-3`}>
                                    {isLoggedIn ? (
                                        <StarFill
                                            className={this.isArticleInFavorites(article) ? 'favorite favorite-on' : 'favorite'}
                                            key={v1()}
                                            onClick={() => this.handleClickFavorite(article)}/>
                                    ) : ('')}
                                    <div>
                                        <div className='source'>{article.source.name}</div>
                                        <h4
                                            style={{cursor: "pointer"}}
                                            onClick={() => this.handleOpenArticle(article)}>
                                            {article.title}
                                        </h4>
                                        <p>{article.description}</p>
                                    </div>
                                    {/* { console.log(this.state.isFavoriteClicked ? 'ON' : 'OFF') } */}

                                </div>
                            </div>
                        )
                    }
                )}
            </div>
        )
    }
}

const mapStateToProps = (state) => {
    return {
        user: state.user
    };
};

function mapDispatchToProps(dispatch) {
    return {
        saveUser: (user) => dispatch(saveUser(user)),
    };
}

const Blocks = connect(mapStateToProps, mapDispatchToProps)(Block);
export default connect()(Blocks);