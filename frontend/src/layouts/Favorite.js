import React, {Component} from 'react';
import Block from '../components/Blocks/Blocks';
import {saveUser} from '../actions/SaveUser'
import {connect} from 'react-redux';
import * as Const from '../constants/Constants'
import {v1} from 'uuid';

class Favorite extends Component {
    constructor(props) {
        super(props);
        this.state = {articles: []};

        const callback = _favorites => this.setState({articles: _favorites});

        Const
            .getFavoritesForUser(this.props.user.user, callback)
            .then(_ => console.log('Favorites loaded'))
    }

    render() {
        return (this.state.articles && this.state.articles.length)
            ? <Block key={v1} articles={this.state.articles} favorites={this.state.articles} isFavPage={true}/>
            : <h2>You did not mark any article as a favorite!</h2>;
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

const Favorites = connect(mapStateToProps, mapDispatchToProps)(Favorite);
export default connect()(Favorites);