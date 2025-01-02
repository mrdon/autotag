import os
from dataclasses import dataclass

from pydantic import BaseModel, Field
from pydantic_ai import Agent, RunContext


@dataclass
class PhotoDependencies:
    photo_path: str
    baseurl: str


class PhotoResult(BaseModel):
    result: str = Field(description='The result of the action(s)')


photo_agent = Agent(
    'openai:gpt-4o',
    deps_type=PhotoDependencies,
    result_type=PhotoResult,
    system_prompt=(
        'You are a photo management agent, who tries to perform the tasks asked of you.'
    ),
)


@photo_agent.system_prompt
async def photo_details(ctx: RunContext[PhotoDependencies]) -> str:
    photo_path = "/".join(ctx.deps.photo_path.split("/")[-3:])
    album_path = "/".join(ctx.deps.photo_path.split("/")[-3:-1])
    photo_url = f"{ctx.deps.baseurl}/{photo_path}"
    album_url = f"{ctx.deps.baseurl}/{album_path}"
    photo_filename = os.path.basename(ctx.deps.photo_path)

    return f"""
    The photo file name is '{photo_filename}.
    The URL to access the photo is at: {photo_url}.
    The URL to access the whole album is at: {album_url}. 
    """


@photo_agent.tool
async def share_album(
    ctx: RunContext[PhotoDependencies], target: str
) -> bool:
    """Shares the photo album to a target such as 'family' or 'julie'. Returns whether
    the share was successful."""

    print("Would have shared album with target: ", target)
    return True


@photo_agent.tool
async def share_photo(
    ctx: RunContext[PhotoDependencies], target: str
) -> bool:
    """Shares the photo to a target such as 'family' or 'julie'. Returns whether
    the share was successful."""

    print("Would have shared photo with target: ", target)
    return True


def run_photo_agent(baseurl: str, photo_path: str, user_prompt: str) -> str:
    deps = PhotoDependencies(photo_path=photo_path, baseurl=baseurl)
    result = photo_agent.run_sync(user_prompt, deps=deps)
    return result.data.result
