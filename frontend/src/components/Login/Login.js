import React, {Component} from 'react';
import {saveUser} from '../../actions/SaveUser';
import {connect} from 'react-redux';
import LoginForm from './LoginForm';
import * as Const from '../../constants/Constants'
import {SERVER_PORT} from "../../constants/Constants";

class Login extends Component {
    constructor(props) {
        super(props);
        this.state = {
            isLoginSucces: false,
            isLoggedIn: false,
            succeded: false,
            submitted: false,
            error: ''
        };

        this.props.saveUser({
            isLoggedIn: false,
            user: {
                userId: {
                    domain: Const.DOMAIN,
                    email: this.props.user.user.email,
                },
                role: this.props.user.user.role,
                username: this.props.user.user.username,
                avatar: this.props.user.user.avatar
            }
        });
    }

    handleChange(event) {
        this.setState({email: event.target.email});
    }

    render() {
        const handleButtonClick = async (email) => {
            this.setState({
                isInProgress: true,
            });

            await fetch(
                'http://localhost:' + SERVER_PORT + '/iob/users/login/' + Const.DOMAIN + '/' + email
            ).then((response) => {
                if (response.status === 200) {
                    this.setState({succeded: true});
                    response.json().then((d) => {
                        const user = d;

                        this.props.saveUser({
                            isLoggedIn: true,
                            user: {
                                userId: {
                                    domain: Const.DOMAIN,
                                    email: email,
                                },
                                role: user.role,
                                userName: user.username,
                                avatar: user.avatar,
                            }
                        });
                        this.setState({isLoggedIn: true, error: ''});
                        window.location.href = '/';
                    });
                } else {
                    document.getElementById('msg-box').classList.remove('d-none');

                    if (email === '') {
                        this.setState({
                            isLoggedIn: false,
                            error: Const.EMPTY_FIELDS,
                        });
                    } else {
                        response.json().then((_) => {
                            this.setState({
                                isLoginSucces: false,
                                error: Const.INCORRECT_FIELDS,
                            });
                        });
                    }
                }
            }).catch((error) => {
                console.error(Const.ERROR_MSG, error.data);
            });
        };

        return (
            <div>
                <LoginForm
                    onButtonClick={handleButtonClick}
                    error={this.state.error}/>
            </div>
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
        saveUser: (user) => dispatch(saveUser(user))
    };
}

const SignIn = connect(mapStateToProps, mapDispatchToProps)(Login);
export default connect()(SignIn);