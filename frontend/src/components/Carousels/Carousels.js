import React, {Component} from 'react';
import Carousel from 'react-bootstrap/Carousel'
import {v1} from 'uuid';
import {createInstance} from '../../actions/CreateInstance'
import {saveUser} from '../../actions/SaveUser'
import {connect} from 'react-redux';
import * as Const from '../../constants/Constants'

import './Carousels.css'

class Carousels extends Component {
    constructor(props) {
        super(props);
        this.props.saveUser({
            user: this.props.user.user
        });
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

    render() {
        return (
            <Carousel className='p-5 mb-4 rounded-3 dark-bg pt-1 pb-1 container-carousel-item' key={v1()}>
                {this.props.articles && this.props.articles.map((article) => {
                        return (
                            <Carousel.Item className='container-fluid inner-carousel-item py-5' key={article.url}>
                                <div className='d-flex justify-content-start hidden-text-container'>
                                    <img
                                        className='d-block carousel-block d-block d-none d-md-block'
                                        src={article.urlToImage}
                                        alt={article.title}
                                        style={{cursor: "pointer"}}
                                        onClick={() => this.handleOpenArticle(article)}/>
                                    <div className='carousel-item-details'>
                                        <h4 className='h4 fw-bold' style={{cursor: "pointer"}}
                                            onClick={() => this.handleOpenArticle(article)}>{article.title}</h4>
                                        <p className=''>{article.description}</p>
                                    </div>
                                </div>
                            </Carousel.Item>
                        )
                    }
                )}
            </Carousel>
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

const CarouselComp = connect(mapStateToProps, mapDispatchToProps)(Carousels);
export default connect()(CarouselComp);