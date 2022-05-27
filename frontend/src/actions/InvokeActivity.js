import {SERVER_PORT} from "../constants/Constants";

export async function invokeActivity(instanceId, userId, invoke_type) {
	const request_body = JSON.stringify({
		type: invoke_type,
		instance: {
			instanceId: instanceId
		},
		invokedBy: {
			userId: userId
		}
	});

	await fetch('http://localhost:' + SERVER_PORT + '/iob/activities', {
		method: 'POST',
		headers: {
			'Content-Type': 'application/json',
			credentials: 'include',
		},
		body: request_body
	}).then((response) => {
		if (response.status === 200) {
			response.json().then((res) => {
				return res;
			}, (error) => {
				console.log(error);
			});
		}
	});

	return null;
}