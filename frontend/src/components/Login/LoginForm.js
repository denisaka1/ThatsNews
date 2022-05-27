import React, {useEffect} from 'react';
import {Form, Button} from 'react-bootstrap'
import * as Const from '../../constants/Constants'
import './Login.css'

export default function SignInForm({onButtonClick, error}) {
    const [Email, setEmail] = React.useState('');
    const [isDisabled, setDiabled] = React.useState(true);
    const handleChange = (event) => {
        setEmail(event.target.value);
    };

    const handleButtonClick = () => onButtonClick(Email);
    useEffect(() => {
        if (Email !== '') {
            setDiabled(false);
        } else {
            setDiabled(true);
        }
    });

    return (
        <div className='flex-grid justify-content-center'>
            <Form className='sign-form'>
                <h1 className='h3 mb-3 fw-normal text-center'>{Const.SIGNIN}</h1>
                <div id='msg-box' className='alert alert-danger mt-2 d-none'>{error}</div>
                <Form.Group className='sign-width mt-2' controlId='formBasicEmail'>
                    <Form.Label className='label'>{Const.EMAIL_ADDRESS_LABEL}</Form.Label>
                    <Form.Control type='email' className='sign-width' placeholder={Const.EMAIL_SAMPLE}
                                  variant='outlined' value={Email} onChange={handleChange}/>
                    <Form.Text className='text-muted'>
                    </Form.Text>
                </Form.Group>
                <Button className='sign-width mt-3' onClick={handleButtonClick}>
                    {Const.SUBMIT}
                </Button>
                <p className='sign-small text-right'>
                    Don`t have an account? <a href='/register'>Sign up free</a>
                </p>
            </Form>
        </div>
    );
}
