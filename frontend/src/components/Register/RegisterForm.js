import React, {useEffect} from 'react';
import {Form, Button} from 'react-bootstrap'
import * as Const from '../../constants/Constants'
import './Register.css'

export default function RegisterForm({onButtonClick, error}) {
    const [Username, setUsername] = React.useState('');
    const [Email, setEmail] = React.useState('');
    const [isUsernameValid, setUsernameValid] = React.useState(false);
    const [isEmailValid, setEmailValid] = React.useState(false);

    const handleUsername = (event) => {
        setUsername(event.target.value);
    };

    const handleEmail = (event) => {
        setEmail(event.target.value);
    };

    const handleButtonClick = () => onButtonClick(Username, Email);
    useEffect(() => {
        if (Username !== '') {
            setUsernameValid(true);
        } else {
            setUsernameValid(false);
        }

        if (Email !== '') {
            setEmailValid(true);
        } else {
            setEmailValid(false);
        }
    });

    return (
        <div className='flex-grid justify-content-center'>
            <Form className='sign-form'>
                <h1 className='h3 mb-3 fw-normal text-center'>{Const.SIGNUP}</h1>
                <div id='msg-box' className='alert alert-danger mt-2 d-none'>{error}</div>
                <Form.Group className='sign-width' controlId='formBasicEmail'>
                    <Form.Label className='label'>{Const.USERNAME_LABEL}</Form.Label>
                    <Form.Control type='text' className='sign-width form-control' variant='outlined' value={Username}
                                  onChange={handleUsername}/>
                    <Form.Text className='text-muted'>
                    </Form.Text>
                </Form.Group>
                <Form.Group className='sign-width mt-2' controlId='formBasicEmail'>
                    <Form.Label className='label'>{Const.EMAIL_ADDRESS_LABEL}</Form.Label>
                    <Form.Control type='email' className='sign-width' placeholder={Const.EMAIL_SAMPLE}
                                  variant='outlined' value={Email} onChange={handleEmail}/>
                    <Form.Text className='text-muted'>
                    </Form.Text>
                </Form.Group>
                <Button className='sign-width mt-3' onClick={handleButtonClick}>
                    {Const.SUBMIT}
                </Button>
                <p className='sign-small text-right'>
                    Already have an account? <a href='/login'>Sign in</a>
                </p>
            </Form>
        </div>
    );
}