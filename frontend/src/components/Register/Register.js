import React, {Component} from 'react'
import {saveUser} from '../../actions/SaveUser';
import {connect} from 'react-redux';
import RegisterForm from './RegisterForm'
import * as Const from '../../constants/Constants'
import {SERVER_PORT} from "../../constants/Constants";

class Register extends Component {
    constructor(props) {
        super(props);
        this.state = {
            isSignup: false,
            error: ''
        };
    }

    handleChange(event) {
        this.setState({email: event.target.email});
        this.setState({name: event.target.username});
    }

    render() {
        const handleButtonClick = async (Username, Email) => {
            const request_body = JSON.stringify({
                email: Email,
                role: 'PLAYER',
                username: Username,
                avatar: 'null'
            });

            await fetch('http://localhost:' + SERVER_PORT + '/iob/users', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    credentials: 'include',
                },
                body: request_body
            }).then(
                (response) => {
                    if (response.status === 200) {
                        let msgBoxClasses = document.getElementById('msg-box').classList;
                        msgBoxClasses.remove('d-none');
                        msgBoxClasses.remove('alert-danger');
                        msgBoxClasses.add('alert-success');

                        this.setState({error: ''})
                        response.json().then((_) => {
                            this.setState({
                                isSignup: true,
                                error: 'You have been successful sign up'
                            });
                            alert('Signup Successful! please login.');
                            window.location.href = "/login";
                        });
                    } else {
                        document.getElementById('msg-box').classList.remove('d-none');

                        if (Username === '' || Email === '') {
                            this.setState({isSignup: false, error: Const.EMPTY_FIELDS});
                        } else {
                            response.json().then((_) => {
                                this.setState({
                                    isSignup: false,
                                    error: Const.INCORRECT_FIELDS
                                });
                                window.location.href('/login');
                            });
                        }
                    }
                }, (error) => {
                    console.log(error);
                }
            );
        };

        return (
            <div>
                <RegisterForm onButtonClick={handleButtonClick} error={this.state.error}/>
            </div>
        )
    }
}

const mapStateToProps = (state) => {
    return {
        user: state.newUser
    };
};

function mapDispatchToProps(dispatch) {
    return {
        saveUser: (newUser) => dispatch(saveUser(newUser))
    };
}

const SignUp = connect(mapStateToProps, mapDispatchToProps)(Register);
export default SignUp;