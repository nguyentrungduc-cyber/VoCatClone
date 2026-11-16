package com.vocatclone.app.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MarkEmailUnread
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vocatclone.app.R
import com.vocatclone.app.ui.theme.VoCatCloneTheme

/**
 * Dải nhắc "hãy xác thực email", hiện trên màn Home khi tài khoản chưa xác thực.
 * Không chặn người dùng dùng app: dữ liệu nằm trong máy và đã lọc theo firebaseUid.
 *
 * Tự kiểm tra lại mỗi khi người dùng quay lại app, vì họ thường rời app sang hộp thư bấm link.
 */
@Composable
fun EmailVerificationBanner(
    modifier: Modifier = Modifier,
    viewModel: EmailVerificationViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.refresh()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    if (!state.show) return

    val note = when {
        state.isSending -> stringResource(R.string.ev_sending)
        state.resendResult == ResendResult.SENT -> stringResource(R.string.ev_sent)
        state.resendResult == ResendResult.FAILED -> stringResource(R.string.ev_send_failed)
        state.email != null -> stringResource(R.string.ev_message_with_email, state.email.orEmpty())
        else -> stringResource(R.string.ev_message)
    }

    Surface(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, top = 12.dp, end = 8.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.MarkEmailUnread,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.ev_title),
                    style = MaterialTheme.typography.titleSmall
                )
                Text(text = note, style = MaterialTheme.typography.bodySmall)
            }
            TextButton(onClick = viewModel::resend, enabled = !state.isSending) {
                Text(stringResource(R.string.ev_resend))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun EmailVerificationBannerPreview() {
    VoCatCloneTheme {
        // Preview vẽ tay vì bản thật cần Firebase.
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(start = 16.dp, top = 12.dp, end = 8.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(Icons.Outlined.MarkEmailUnread, contentDescription = null, modifier = Modifier.size(24.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.ev_title), style = MaterialTheme.typography.titleSmall)
                    Text(
                        stringResource(R.string.ev_message_with_email, "an@example.com"),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                TextButton(onClick = {}) { Text(stringResource(R.string.ev_resend)) }
            }
        }
    }
}
