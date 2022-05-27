import React, {Component} from 'react'
import {Nav, Button} from 'react-bootstrap';
import {saveUser} from '../../actions/SaveUser'
import {connect} from 'react-redux';
import {v1} from 'uuid';

class UserNavbar extends Component {
    constructor(props) {
        super(props);

        this.props.saveUser({
            user: this.props.user.user
        });
    }

    onLogoutClick() {
        if (this.props.user.isLoggedIn) {
            this.props.user.isLoggedIn = false;
            localStorage.clear();
            window.location.href = '/';
        }
    }

    render() {
        const isLoggedIn = this.props.user.isLoggedIn

        return (
            <Nav className='justify-content-last'>
                {isLoggedIn ? ([
                    <Nav.Link href='/favorites'>Favorites</Nav.Link>,
                    <Nav.Link href='' onClick={() => this.onLogoutClick() }>Logout</Nav.Link>]
                ) : (
                    <Nav.Link href='/login'>Sign in</Nav.Link>
                )}
            </Nav>
        );
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

const UserNav = connect(mapStateToProps, mapDispatchToProps)(UserNavbar);

export default connect()(UserNav);